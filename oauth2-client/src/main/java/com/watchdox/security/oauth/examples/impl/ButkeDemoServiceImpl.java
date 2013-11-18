package com.watchdox.security.oauth.examples.impl;

import com.watchdox.security.oauth.examples.ButkeDemoService;
import org.springframework.web.client.RestOperations;

import java.net.URI;


public class ButkeDemoServiceImpl implements ButkeDemoService
{

	private RestOperations butkeDemoRestTemplate;

    private String demoUrl;

    public void setButkeDemoRestTemplate(RestOperations butkeDemoRestTemplate)
    {
        this.butkeDemoRestTemplate = butkeDemoRestTemplate;
    }

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
