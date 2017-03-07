package com.ohadr.auth_flows.core.jdbc;

import java.sql.Types;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import com.ohadr.auth_flows.interfaces.LinksRepository;

public class JdbcLinksRepositoryImpl implements LinksRepository, InitializingBean 
{
	private static Logger log = Logger.getLogger(JdbcLinksRepositoryImpl.class);

	private static final String TABLE_NAME = "links";
	private static final String LINK_INSERT_STATEMENT = "insert into " + TABLE_NAME + "(LINK) values (?)";
	private static final String LINK_REMOVAL_STATEMENT = "DELETE FROM " + TABLE_NAME + " WHERE LINK=?";

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
	public void addLink(String link) 
	{
		int rowsUpdated = jdbcTemplate.update(LINK_INSERT_STATEMENT,
				new Object[] { link },
				new int[] { Types.VARCHAR });
		if(rowsUpdated != 1)
		{
			throw new RuntimeException("could not insert new entry to DB");
		}
	}

	@Override
	public boolean removeLink(String link) 
	{
		int rowsUpdated = jdbcTemplate.update(LINK_REMOVAL_STATEMENT,
				new Object[] { link },
				new int[] { Types.VARCHAR });
		if(rowsUpdated != 1)
		{
			return false;
		}
		return true;
	}

}
