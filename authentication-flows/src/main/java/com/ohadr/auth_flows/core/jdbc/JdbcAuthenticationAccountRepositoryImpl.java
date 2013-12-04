package com.ohadr.auth_flows.core.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import com.ohadr.auth_flows.core.AbstractAuthenticationAccountRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationUser;
import com.ohadr.auth_flows.mocks.InMemoryAuthenticationUserImpl;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;


//@Repository
public class JdbcAuthenticationAccountRepositoryImpl extends AbstractAuthenticationAccountRepository
		implements InitializingBean
{
	private static Logger log = Logger.getLogger(JdbcAuthenticationAccountRepositoryImpl.class);

	private static final String TABLE_NAME = "users";

	private static final String AUTHENTICATION_USER_FIELDS = "USERNAME, password, enabled, "
			+ "LOGIN_ATTEMPTS_COUNTER,"
			+ "LAST_PSWD_CHANGE_DATE";

	private static final String DEFAULT_USER_INSERT_STATEMENT = "insert into " + TABLE_NAME + "(" + AUTHENTICATION_USER_FIELDS
			+ ") values (?,?,?,?,?)";

	private static final String DEFAULT_USER_SELECT_STATEMENT = "select " + AUTHENTICATION_USER_FIELDS
			+ " from " + TABLE_NAME + " where USERNAME = ?";

	private static final String DEFAULT_USER_DELETE_STATEMENT = "delete from " + TABLE_NAME + " where USERNAME = ?";
	
	//upon settign new password, set also the "last changed":
	private static final String DEFAULT_UPDATE_PASSWORD_STATEMENT = "update " + TABLE_NAME + 
			" set password = ? and LAST_PSWD_CHANGE_DATE = ? where USERNAME = ?";
	
	private static final String DEFAULT_UPDATE_ACTIVATED_STATEMENT = "update " + TABLE_NAME + 
			" set enabled = ? where USERNAME = ?";

	private static final String DEFAULT_UPDATE_ATTEMPTS_CNTR_STATEMENT = null;

	
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
	public AccountState createAccount(String email, String encodedPassword
			//NOT IMPLEMENTED: String secretQuestion, String encodedAnswer
			)
	{
		int rowsUpdated = jdbcTemplate.update(DEFAULT_USER_INSERT_STATEMENT,
				new Object[] { email, encodedPassword, false, 0, new Date( System.currentTimeMillis()) },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.BOOLEAN, Types.INTEGER, Types.DATE });

		if(rowsUpdated != 1)
		{
			throw new RuntimeException("could not insert new entry to DB");
		}
		
		return AccountState.OK;

	}

	@Override
	public AuthenticationUser getUser(String email) 
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
		}


		return userFromDB;
	}

	@Override
	public void deleteAccount(String email)
	{
		int count = jdbcTemplate.update(DEFAULT_USER_DELETE_STATEMENT, email);
		if (count != 1)
		{
			throw new NoSuchElementException("No user with email: " + email);
		}
	}

	@Override
	public boolean changePassword(String username, String newEncodedPassword) 
	{
		int count = jdbcTemplate.update(DEFAULT_UPDATE_PASSWORD_STATEMENT, 
				newEncodedPassword,
				new Date( System.currentTimeMillis()),
				username);
		if (count != 1)
		{
			throw new NoSuchElementException("No user with email: " + username);
		}
		return true;
	}

	//TODO: impl from DB
	@Override
	public AuthenticationPolicy getAuthenticationPolicy()
	{
		// TODO read from DB!!!
		AuthenticationPolicy policy = new AuthenticationPolicy();
		policy.setMaxPasswordEntryAttempts( 5 );
		policy.setPasswordMaxLength( 8 );
		policy.setRememberMeTokenValidityInDays( 30 );

		return policy;
	}

	
	
	private static class AuthenticationUserRowMapper implements RowMapper<AuthenticationUser>
	{
		public AuthenticationUser mapRow(ResultSet rs, int rowNum) throws SQLException 
		{
			AuthenticationUser user = new InMemoryAuthenticationUserImpl(
					rs.getString(1),		//username / email
					rs.getString(2),		//password
					rs.getBoolean(3)		//activated?
					);

			user.setLoginAttemptsCounter(rs.getInt(4));
			user.setPasswordLastChangeDate(rs.getDate(5));
			
			return user;
		}
	}


	@Override
	public boolean setPassword(String email, String newPassword) 
	{
		return changePassword(email, newPassword);
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

}
