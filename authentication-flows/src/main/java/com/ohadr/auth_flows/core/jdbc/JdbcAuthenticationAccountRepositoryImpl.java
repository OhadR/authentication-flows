package com.ohadr.auth_flows.core.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ohadr.auth_flows.core.AbstractAuthenticationAccountRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationUser;
import com.ohadr.auth_flows.mocks.InMemoryAuthenticationUserImpl;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;


@Repository
public class JdbcAuthenticationAccountRepositoryImpl extends AbstractAuthenticationAccountRepository
		implements InitializingBean
{
	private static final String TABLE_NAME = "auth_user";

	private static final String AUTHENTICATION_USER_FIELDS = null;

	private static final String DEFAULT_USER_INSERT_STATEMENT = "insert into " + TABLE_NAME + "(" + AUTHENTICATION_USER_FIELDS
			+ ") values (?,?,?,?,?,?,?,?,?,?)";

	private static final String DEFAULT_USER_SELECT_STATEMENT = "select " + AUTHENTICATION_USER_FIELDS
			+ " from oauth_client_details where client_id = ?";

	private static final String DEFAULT_USER_DELETE_STATEMENT = "delete from " + TABLE_NAME + " where EMAIL = ?";

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

//	protected EntityManager entityManager;

	@Override
	public void afterPropertiesSet() throws Exception 
	{
		Assert.notNull(dataSource, "DataSource required");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

/*	@PersistenceContext(unitName = "oauth")
	public void setEntityManager(EntityManager entityManager)
	{
		this.entityManager = entityManager;
	}
*/
	@Override
	public AccountState createAccount(String email, String encodedPassword
			//NOT IMPLEMENTED: String secretQuestion, String encodedAnswer
			)
	{
		AuthenticationUser user = new JdbcAuthenticationUserImpl();
		user.setEmail(email);
		user.setPassword(encodedPassword);
		user.setActivated( false );
		user.setLoginAttemptsCounter(0);
		user.setPasswordLastChangeDate(new Date( System.currentTimeMillis() ));
//		entityManager.persist(user);

		jdbcTemplate.update(DEFAULT_USER_INSERT_STATEMENT,
				new Object[] { email, encodedPassword, false, 0, null },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.BOOLEAN, Types.INTEGER, Types.DATE });

		
		return AccountState.OK;

	}

	@Override
	public AuthenticationUser getUser(String email) 
	{
		AuthenticationUser userFromDB = null;
		try
		{
			userFromDB = jdbcTemplate.queryForObject(DEFAULT_USER_SELECT_STATEMENT, 
					new AuthenticationUserRowMapper(), email);
		}
		catch (EmptyResultDataAccessException e) 
		{
			throw new NoSuchElementException("No user with email: " + email);
//			log.info();
		}


		return userFromDB;
	}

	@Override
	public void deleteOAuthAccount(String email)
	{
		AuthenticationUser user = getUser(email);

		int count = jdbcTemplate.update(DEFAULT_USER_DELETE_STATEMENT, email);
		if (count != 1) {
			throw new NoSuchElementException("No user with email: " + email);
		}
	}

	@Override
	public boolean changePassword(String username, String newEncodedPassword) 
	{
		AuthenticationUser user = getUser(username);
		if(user != null)
		{
			user.setPassword(newEncodedPassword);
			user.setPasswordLastChangeDate(new Date( System.currentTimeMillis() ));
			return true;
		}
		else
		{
			return false;
		}
	}

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
//		private ObjectMapper mapper = new ObjectMapper();

		public AuthenticationUser mapRow(ResultSet rs, int rowNum) throws SQLException 
		{
			AuthenticationUser user = new InMemoryAuthenticationUserImpl();
/*			user.setClientSecret(rs.getString(2));
			if (rs.getObject(8) != null) {
				user.setAccessTokenValiditySeconds(rs.getInt(8));
			}
			if (rs.getObject(9) != null) {
				user.setRefreshTokenValiditySeconds(rs.getInt(9));
			}
			String json = rs.getString(10);
			if (json != null) {
				try {
					@SuppressWarnings("unchecked")
					Map<String, Object> additionalInformation = mapper.readValue(json, Map.class);
					user.setAdditionalInformation(additionalInformation);
				}
				catch (Exception e) {
					logger.warn("Could not decode JSON for additional information: " + user, e);
				}
			}*/
			return user;
		}
	}

}
