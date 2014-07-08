package com.ohadr.auth_flows.core.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import com.ohadr.auth_flows.core.AbstractAuthenticationAccountRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationUser;
import com.ohadr.auth_flows.mocks.InMemoryAuthenticationUserImpl;


public class JdbcAuthenticationAccountRepositoryImpl extends AbstractAuthenticationAccountRepository
		implements InitializingBean
{
	private static Logger log = Logger.getLogger(JdbcAuthenticationAccountRepositoryImpl.class);

	private static final String TABLE_NAME = "users";

	private static final String AUTHENTICATION_USER_FIELDS = "USERNAME, password, enabled, "
			+ "LOGIN_ATTEMPTS_COUNTER,"
			+ "LAST_PSWD_CHANGE_DATE,"
			+ "authorities";

	private static final String DEFAULT_USER_INSERT_STATEMENT = "insert into " + TABLE_NAME + "(" + AUTHENTICATION_USER_FIELDS
			+ ") values (?,?,?,?,?,?)";

	private static final String DEFAULT_USER_SELECT_STATEMENT = "select " + AUTHENTICATION_USER_FIELDS
			+ " from " + TABLE_NAME + " where USERNAME = ?";

	private static final String DEFAULT_USER_DELETE_STATEMENT = "delete from " + TABLE_NAME + " where USERNAME = ?";
	
	//upon settign new password, set also the "last changed":
	private static final String DEFAULT_UPDATE_PASSWORD_STATEMENT = "update " + TABLE_NAME + 
			" set password = ?, LAST_PSWD_CHANGE_DATE = ? where USERNAME = ?";
	
	private static final String DEFAULT_UPDATE_ACTIVATED_STATEMENT = "update " + TABLE_NAME + 
			" set enabled = ? where USERNAME = ?";

	private static final String DEFAULT_UPDATE_ATTEMPTS_CNTR_STATEMENT = "update " + TABLE_NAME +
			" set LOGIN_ATTEMPTS_COUNTER = ? where USERNAME = ?";

	private static final String DEFAULT_UPDATE_AUTHORITY_STATEMENT = "update " + TABLE_NAME + 
			" set authorities = ? where USERNAME = ?";

	
	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;


	
	@Override
	public void afterPropertiesSet() throws Exception 
	{
		Assert.notNull(dataSource, "DataSource required");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}


	@Override
	public void createUser(UserDetails user)
	{
		AuthenticationUser authUser = (AuthenticationUser) user;
		int rowsUpdated = jdbcTemplate.update(DEFAULT_USER_INSERT_STATEMENT,
				new Object[] { authUser.getUsername(),
					authUser.getPassword(),
					false,
					authUser.getLoginAttemptsLeft(), 
					new Date( System.currentTimeMillis()),
					user.getAuthorities()},
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.BOOLEAN, Types.INTEGER, Types.DATE, Types.VARCHAR });

		if(rowsUpdated != 1)
		{
			throw new RuntimeException("could not insert new entry to DB");
		}
	}

	
	@Override
	public AuthenticationUser loadUserByUsername(String email) throws UsernameNotFoundException
	{
		AuthenticationUser userFromDB = null;
		try
		{
			log.info("query: " + DEFAULT_USER_SELECT_STATEMENT + " " + email);
			userFromDB = jdbcTemplate.queryForObject(DEFAULT_USER_SELECT_STATEMENT, 
					new AuthenticationUserRowMapper(), email);
		}
		catch (EmptyResultDataAccessException e) 
		{
			log.info("no record was found for email=" + email);
//			throw new NoSuchElementException("No user with email: " + email);
			throw new UsernameNotFoundException( email );
		}


		return userFromDB;
	}

	@Override
	public void deleteUser(String email)
	{
		int count = jdbcTemplate.update(DEFAULT_USER_DELETE_STATEMENT, email);
		if (count != 1)
		{
			throw new NoSuchElementException("No user with email: " + email);
		}
	}

	@Override
	public void changePassword(String username, String newEncodedPassword) 
	{
		int count = jdbcTemplate.update(DEFAULT_UPDATE_PASSWORD_STATEMENT, 
				newEncodedPassword,
				new Date( System.currentTimeMillis()),
				username);
		if (count != 1)
		{
			throw new NoSuchElementException("No user with email: " + username);
		}
	}

    
	private static class AuthenticationUserRowMapper implements RowMapper<AuthenticationUser>
	{
		public AuthenticationUser mapRow(ResultSet rs, int rowNum) throws SQLException 
		{
			String roleName = rs.getString(8);			//column 8 : authorities
			GrantedAuthority userAuth = new SimpleGrantedAuthority(roleName);
			Set<GrantedAuthority> authSet = new HashSet<GrantedAuthority>();
			authSet.add(userAuth);
			
			AuthenticationUser user = new InMemoryAuthenticationUserImpl(
					rs.getString(1),		//username / email
					rs.getString(2),		//password
					rs.getBoolean(3),		//activated?
					rs.getInt(4),			//attempts left
					rs.getDate(5),			//PasswordLastChangeDate
					rs.getString(6),		//firstName
					rs.getString(7),		//lastName
					authSet					//authorities
					);
			
			return user;
		}
	}


	@Override
	public void setPassword(String email, String newPassword) 
	{
		changePassword(email, newPassword);
	}

	/******************************************************************/	
	@Override
	public void setEnabled(String email) 
	{
		setEnabledFlag(email, true);
	}

	@Override
	public void setDisabled(String email) 
	{
		setEnabledFlag(email, false);
	}

	@Override
	protected void setEnabledFlag(String email, boolean flag) 
	{
		int count = jdbcTemplate.update(DEFAULT_UPDATE_ACTIVATED_STATEMENT, flag, email);
		if (count != 1)
		{
			throw new NoSuchElementException("No user with email: " + email);
		}
	}

	@Override
	protected void updateLoginAttemptsCounter(String email, int attempts) 
	{
		int count = jdbcTemplate.update(DEFAULT_UPDATE_ATTEMPTS_CNTR_STATEMENT, attempts, email);
		if (count != 1)
		{
			throw new NoSuchElementException("No user with email: " + email);
		}
	}


	@Override
	public void updateUser(UserDetails user)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean userExists(String username)
	{
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void setAuthority(String username, String authority)
	{
		int count = jdbcTemplate.update( DEFAULT_UPDATE_AUTHORITY_STATEMENT, authority, username);
		if ( count != 1 )
		{
			throw new NoSuchElementException("No user with email: " + username);
		}
	}

}