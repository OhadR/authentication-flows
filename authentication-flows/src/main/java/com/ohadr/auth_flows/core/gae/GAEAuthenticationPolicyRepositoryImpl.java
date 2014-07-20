package com.ohadr.auth_flows.core.gae;

import org.apache.log4j.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.ohadr.auth_flows.interfaces.AuthenticationPolicyRepository;
import com.ohadr.auth_flows.types.AuthenticationPolicy;

public class GAEAuthenticationPolicyRepositoryImpl implements
		AuthenticationPolicyRepository
{
	private static final String PASSWORD_MIN_LENGTH = "password";
	private static final String PASSWORD_MAX_LENGTH = "loginAttemptsLeft";
	private static final String PASSWORD_MIN_UPCASE_CHARS = "enabled";
	private static final String PASSWORD_MIN_LOCASE_CHARS = "lastPasswordChangeDate";
	private static final String PASSWORD_MIN_NUMERALS = "firstName";
	private static final String PASSWORD_MIN_SPECIAL_SYMBOLS = "lastName";
	private static final String PASSWORD_BLACKLIST = "authorities";
	private static final String MAX_PASSWORD_ENTRY_ATTEMPTS = "firstName";
	private static final String PASSWORD_LIFE_IN_DAYS = "lastName";
	private static final String REMEMBER_ME_VALIDITY_IN_DAYS = "authorities";

	
	private static final String AUTH_FLOWS_POLICIES_DB_KIND = "authentication-flows-policies";

	private static Logger log = Logger.getLogger(GAEAuthenticationPolicyRepositoryImpl.class);

	private DatastoreService datastore;
	
	public GAEAuthenticationPolicyRepositoryImpl()
	{
		datastore = DatastoreServiceFactory.getDatastoreService();
	}

	@Override
	public AuthenticationPolicy getDefaultAuthenticationPolicy()
	{
		int settings = 1;
		return getAuthenticationPolicy( settings );
	}

	@Override
	public AuthenticationPolicy getAuthenticationPolicy(int settingsId)
	{
		Key userKey = KeyFactory.createKey(AUTH_FLOWS_POLICIES_DB_KIND, settingsId);
		Entity entity;
		try 
		{
			entity = datastore.get(userKey);
			log.debug("got entity of " + settingsId + ": " + entity);
		} 
		catch (EntityNotFoundException e) 
		{
			log.error("entity of " + settingsId + " not found");
			return null;
		}

		AuthenticationPolicy policy = new AuthenticationPolicy(
			readProperty(entity, PASSWORD_MIN_LENGTH),					//passwordMinLength
			readProperty(entity, PASSWORD_MAX_LENGTH),					//passwordMaxLength
			readProperty(entity, PASSWORD_MIN_UPCASE_CHARS),			//passwordMinUpCaseChars
			readProperty(entity, PASSWORD_MIN_LOCASE_CHARS),			//passwordMinLoCaseChars 
			readProperty(entity, PASSWORD_MIN_NUMERALS),				//digits
			readProperty(entity, PASSWORD_MIN_SPECIAL_SYMBOLS),			//special symbols
			(String) entity.getProperty( PASSWORD_BLACKLIST ),					//passwordBlackList
			readProperty(entity, MAX_PASSWORD_ENTRY_ATTEMPTS),
			readProperty(entity, PASSWORD_LIFE_IN_DAYS),				//passwordLifeInDays
			readProperty(entity, REMEMBER_ME_VALIDITY_IN_DAYS)			//rememberMeTokenValidityInDays
				);

		return policy;
	}
	
	private int readProperty(Entity entity, String property)
	{
		int retVal = 0;
		Object obj = entity.getProperty( property );
		if( null != obj )
		{
			//"hack"  convert Object to int:
			retVal = new Integer( obj.toString() );
		}
		return retVal;
	}

}
