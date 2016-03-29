package com.ohadr.security.oauth.examples.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestOperations;

import com.ohadr.security.oauth.examples.DemoService;

import java.net.URI;


public class DemoServiceImpl implements DemoService
{
	static {
	    //for localhost testing only
	    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
	    new javax.net.ssl.HostnameVerifier(){

	        public boolean verify(String hostname,
	                javax.net.ssl.SSLSession sslSession) {
	            if (hostname.equals("localhost")) {
	                return true;
	            }
	            return false;
	        }
	    });
	}

	@Autowired
	private RestOperations butkeDemoRestTemplate;

    private String demoUrl;


    public void setDemoUrl(String demoUrl)
    {
        this.demoUrl = demoUrl;
    }

    @Override
    public String getTrustedMessage()
    {
        String demo = butkeDemoRestTemplate.getForObject(URI.create(demoUrl), String.class);
        return demo;
    }
}
