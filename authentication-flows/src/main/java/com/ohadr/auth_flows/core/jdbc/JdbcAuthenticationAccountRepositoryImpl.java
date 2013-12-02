package com.ohadr.auth_flows.core.jdbc;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.ohadr.auth_flows.core.AbstractAuthenticationAccountRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationUser;
import com.ohadr.auth_flows.mocks.InMemoryAuthenticationUserImpl;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;


@Repository
public class JdbcAuthenticationAccountRepositoryImpl extends AbstractAuthenticationAccountRepository 
{
	protected EntityManager entityManager;

	@PersistenceContext(unitName = "oauth")
	public void setEntityManager(EntityManager entityManager)
	{
		this.entityManager = entityManager;
	}

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
		entityManager.persist(user);

		return AccountState.OK;

	}

	@Override
	public AuthenticationUser getUser(String email) 
	{
		return entityManager.find(JdbcAuthenticationUserImpl.class, email);
	}

	@Override
	public void deleteOAuthAccount(String email)
	{
		AuthenticationUser user = getUser(email);
		entityManager.remove(user);
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
	public AuthenticationPolicy getAuthenticationPolicy() {
		// TODO Auto-generated method stub
		return null;
	}

}
