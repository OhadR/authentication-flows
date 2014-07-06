package com.ohadr.auth_flows.core.gae;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.appengine.api.datastore.*;
import com.ohadr.auth_flows.core.AbstractAuthenticationAccountRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationUser;
import com.ohadr.auth_flows.mocks.InMemoryAuthenticationUserImpl;


public class GAEAuthenticationAccountRepositoryImpl extends
		AbstractAuthenticationAccountRepository 
{
	private static final String PASSWORD_PROP_NAME = "password";
	private static final String LOGIN_ATTEMPTS_LEFT_PROP_NAME = "loginAttemptsLeft";
	private static final String ENABLED_PROP_NAME = "enabled";
	private static final String LAST_PSWD_CHANGE_DATE_PROP_NAME = "lastPasswordChangeDate";
	private static final String FIRST_NAME_PROP_NAME = "firstName";
	private static final String LAST_NAME_PROP_NAME = "lastName";
	private static final String AUTHORITIES_PROP_NAME = "authorities";

	private static final String AUTH_FLOWS_USER_DB_KIND = "authentication-flows-user";


	private static Logger log = Logger.getLogger(GAEAuthenticationAccountRepositoryImpl.class);

	private DatastoreService datastore;
	
	public GAEAuthenticationAccountRepositoryImpl()
	{
		datastore = DatastoreServiceFactory.getDatastoreService();
	}

	@Override
	public void setDisabled(String email) 
	{
		setEnabledFlag(email, false);
	}

	@Override
	public void setEnabled(String email) 
	{
		setEnabledFlag(email, true);
	}

	@Override
	public void setPassword(String username, String newEncodedPassword)
	{
		changePassword(username, newEncodedPassword);
	}

	@Override
	public void createUser(UserDetails user) 
	{
		AuthenticationUser authUser = (AuthenticationUser) user;

		Entity dbUser = new Entity(AUTH_FLOWS_USER_DB_KIND, user.getUsername());		//the username is the key

		dbUser.setProperty("username", user.getUsername());
		dbUser.setProperty(PASSWORD_PROP_NAME, user.getPassword());
		dbUser.setProperty(ENABLED_PROP_NAME, user.isEnabled());
		dbUser.setProperty(LOGIN_ATTEMPTS_LEFT_PROP_NAME, authUser.getLoginAttemptsLeft());
		dbUser.setProperty(LAST_PSWD_CHANGE_DATE_PROP_NAME, new Date( System.currentTimeMillis()) );

		dbUser.setProperty( FIRST_NAME_PROP_NAME, authUser.getFirstName() );
		dbUser.setProperty( LAST_NAME_PROP_NAME, authUser.getLastName() );

		dbUser.setProperty(AUTHORITIES_PROP_NAME, "ROLE_USER" );

		datastore.put(dbUser);	
	}

	@Override
	public void updateUser(UserDetails user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteUser(String username) 
	{
		Key userKey = KeyFactory.createKey(AUTH_FLOWS_USER_DB_KIND, username);
		datastore.delete(userKey);
		
	}


	@Override
	public boolean userExists(String username) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException
	{
		//issue #34: if username is null, login fails and then we try to AuthenticationFlowsProcessorImpl.setLoginFailureForUser()
		//that causes IllegalArgumentException.
		if( username == null || username.isEmpty() )
		{
			log.error("name cannot be null or empty");
			throw new UsernameNotFoundException( "name cannot be null or empty" );
		}
		
		Key userKey = KeyFactory.createKey(AUTH_FLOWS_USER_DB_KIND, username);
		Entity entity;
		try 
		{
			entity = datastore.get(userKey);
			log.debug("got entity of " + username + ": " + entity);
		} 
		catch (EntityNotFoundException e) 
		{
			log.error("entity of " + username + " not found");
			throw new UsernameNotFoundException(username, e);
		}
		
		boolean isEnabled = false;
		Object isEnabledObj = entity.getProperty(ENABLED_PROP_NAME);
		if( null != isEnabledObj )
		{
			isEnabled = (Boolean)isEnabledObj;
		}
		int loginAttemptsLeft = 0;
		Object loginAttemptsLeftObj = entity.getProperty(LOGIN_ATTEMPTS_LEFT_PROP_NAME);
		if( null != loginAttemptsLeftObj )
		{
			//"hack"  convert Object to int:
			loginAttemptsLeft = new Integer(loginAttemptsLeftObj.toString());
		}

		String roleName = (String)entity.getProperty(AUTHORITIES_PROP_NAME);
		GrantedAuthority userAuth = new SimpleGrantedAuthority(roleName);
		String firstName = (String)entity.getProperty(FIRST_NAME_PROP_NAME);
		String lastName = (String)entity.getProperty(LAST_NAME_PROP_NAME);
		Collection<GrantedAuthority>  authSet = new HashSet<GrantedAuthority>();
		authSet.add(userAuth);
		
		return new InMemoryAuthenticationUserImpl(
						username, 
						(String)entity.getProperty(PASSWORD_PROP_NAME),
						isEnabled,
						loginAttemptsLeft,
						(Date)entity.getProperty(LAST_PSWD_CHANGE_DATE_PROP_NAME),
						firstName,
						lastName,
						authSet);
		
	}


	@Override
	protected void setEnabledFlag(String username, boolean flag) throws NoSuchElementException
	{
		Key userKey = KeyFactory.createKey(AUTH_FLOWS_USER_DB_KIND, username);
		Entity entity;
		try 
		{
			entity = datastore.get(userKey);
			log.debug("got entity of " + username + ": " + entity);
		} 
		catch (EntityNotFoundException e) 
		{
			log.error("entity of " + username + " not found");
			throw new NoSuchElementException(e.getMessage());
		}
		
		entity.setProperty(ENABLED_PROP_NAME, flag);				
		datastore.put(entity);	
	}

	@Override
	protected void updateLoginAttemptsCounter(String username, int attempts) throws NoSuchElementException
	{
//		FlowsUtil.logStackTrace( log );

		Key userKey = KeyFactory.createKey(AUTH_FLOWS_USER_DB_KIND, username);
		Entity entity;
		try 
		{
			entity = datastore.get(userKey);
			log.debug("got entity of " + username + ": " + entity);
		} 
		catch (EntityNotFoundException e) 
		{
			log.error("entity of " + username + " not found");
			throw new NoSuchElementException(e.getMessage());
		}
		
		entity.setProperty(LOGIN_ATTEMPTS_LEFT_PROP_NAME, attempts);				
		datastore.put(entity);	
	}

	@Override
	public void changePassword(String username, String newEncodedPassword) 
	{
		Key userKey = KeyFactory.createKey(AUTH_FLOWS_USER_DB_KIND, username);
		Entity entity;
		try 
		{
			entity = datastore.get(userKey);
			log.debug("got entity of " + username + ": " + entity);
		} 
		catch (EntityNotFoundException e) 
		{
			log.error("entity of " + username + " not found");
			throw new NoSuchElementException(e.getMessage());
		}
		
		entity.setProperty(LAST_PSWD_CHANGE_DATE_PROP_NAME, new Date( System.currentTimeMillis()));
		entity.setProperty(PASSWORD_PROP_NAME, newEncodedPassword);
		datastore.put(entity);	
	}

}
