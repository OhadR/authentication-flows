package com.ohadr.auth_flows.interfaces;

public interface LinksRepository 
{
	void addLink(String link);
	
	/**
	 * 
	 * @param link- the link to search
	 * @return true if link was found (and removed). false otherwise.
	 */
	boolean removeLink(String link);

}
