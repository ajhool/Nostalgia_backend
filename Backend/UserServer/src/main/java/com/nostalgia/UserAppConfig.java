package com.nostalgia;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

public class UserAppConfig extends Configuration{
	
	@Valid
	@NotNull
	@JsonProperty("UserCouch")
	private CouchbaseConfig userServConfig = new  CouchbaseConfig();

	public CouchbaseConfig getUserServerConfig() {
		return userServConfig;
	}
	
	@Valid
	@NotNull
	@JsonProperty("iconSvcConn")
	private IconServiceConfig icConfig = new IconServiceConfig();
	
	
	public IconServiceConfig getIconServiceConfig(){
		return icConfig;
	}
	
	@Valid
	@NotNull
	@JsonProperty("SyncServer")
	private SyncConfig syncConfig = new SyncConfig();

	public SyncConfig getSyncConfig() {
		return syncConfig;
	}

	@Valid
	@NotNull
	@JsonProperty("jerseyClient")
	private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

	
	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClient;
	}

	
	@Valid
	@NotNull
	@JsonProperty("LocationCouch")
	private CouchbaseConfig locationServConfig = new  CouchbaseConfig();

	public CouchbaseConfig getLocationServerConfig() {
		return locationServConfig;
	}
	
	@Valid
	@NotNull
	@JsonProperty("VideoCouch")
	private CouchbaseConfig videoServConfig = new  CouchbaseConfig();

	public CouchbaseConfig getVideoCouchConfig() {
		return videoServConfig;
	}
	
	
}