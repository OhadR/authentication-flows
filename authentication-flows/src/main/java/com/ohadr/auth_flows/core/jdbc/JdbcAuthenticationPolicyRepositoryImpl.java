package com.ohadr.auth_flows.core.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import com.ohadr.auth_flows.interfaces.AuthenticationPolicyRepository;
import com.ohadr.auth_flows.types.AuthenticationPolicy;

/**
 * AuthenticationPolicy table:
 * in this version, there is only getters from this table. in future versions, admin consloe can
 * change and set values to this table.
 * 
 * @author OhadR
 *
 */
public class JdbcAuthenticationPolicyRepositoryImpl implements 
	AuthenticationPolicyRepository, InitializingBean
{
	private static Logger log = Logger.getLogger(JdbcAuthenticationPolicyRepositoryImpl.class);

	private static final String TABLE_NAME = "policy";

	private static final String AUTHENTICATION_POLICY_FIELDS = 	
//		"POLICY_ID," +
		 "PASSWORD_MIN_LENGTH,"
		+ "PASSWORD_MAX_LENGTH," 
		+ "PASSWORD_MIN_UPCASE_CHARS," 
		+ "PASSWORD_MIN_LOCASE_CHARS," 
		+ "PASSWORD_MIN_NUMERALS," 
		+ "PASSWORD_MIN_SPECIAL_SYMBOLS,"
		+ "PASSWORD_BLACKLIST,"
		+ "MAX_PASSWORD_ENTRY_ATTEMPTS," 
		+ "PASSWORD_LIFE_IN_DAYS," 
		+ "REMEMBER_ME_VALIDITY_IN_DAYS";

	private static final String DEFAULT_USER_SELECT_STATEMENT = "select " + AUTHENTICATION_POLICY_FIELDS
			+ " from " + TABLE_NAME + " where POLICY_ID = ?";


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
	public AuthenticationPolicy getAuthenticationPolicy()
	{
		int settings = 1;
		AuthenticationPolicy policy = null;

		try
		{
			log.info("query: " + DEFAULT_USER_SELECT_STATEMENT + " " + settings);
			policy = jdbcTemplate.queryForObject(DEFAULT_USER_SELECT_STATEMENT, 
					new AuthenticationPolicyRowMapper(), settings);
		}
		catch (EmptyResultDataAccessException e) 
		{
			log.info("no record was found for settings=" + settings);
//			throw new NoSuchElementException("No user with email: " + email);
		}


		return policy;
		
	}
	
	
	
	private static class AuthenticationPolicyRowMapper implements RowMapper<AuthenticationPolicy>
	{
		public AuthenticationPolicy mapRow(ResultSet rs, int rowNum) throws SQLException 
		{
			AuthenticationPolicy user = new AuthenticationPolicy(
					rs.getInt(1),		//
					rs.getInt(2),		//
					rs.getInt(3),		//
					rs.getInt(4),			//
					rs.getInt(5),			//
					rs.getInt(6),			//
					rs.getString(7),			//
					rs.getInt(8),			//
					rs.getInt(9),			//
					rs.getInt(10)			//
					);
			
			return user;
		}
	}


	

}
