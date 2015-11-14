package com.nostalgia.webserver.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;


public class WebServerConfig extends Configuration{
	
	@Valid
	@NotNull
	@JsonProperty("webserver")
	private WebServerGeneralConfig wsConfig = new WebServerGeneralConfig();
	
	
	public WebServerGeneralConfig getWsConfig() {
		return wsConfig;
	}

	@Valid
	@NotNull
	@JsonProperty("jerseyClient")
	private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

	
	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClient;
	}
}
