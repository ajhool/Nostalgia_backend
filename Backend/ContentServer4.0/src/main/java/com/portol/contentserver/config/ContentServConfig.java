package com.portol.contentserver.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;


public class ContentServConfig extends Configuration{

	@Valid
	@NotNull
	@JsonProperty("contentDB")
	public MongoConfig mongoContent = new MongoConfig();

	@Valid
	@NotNull
	@JsonProperty("dataDB")
	public MongoConfig mongoDataDB = new MongoConfig();
	

	@Valid
	@NotNull
	@JsonProperty("metadataDB")
	public MongoConfig mongoSplash = new MongoConfig();
	
	@Valid
	@NotNull
	@JsonProperty("categoryDB")
	public MongoConfig categoryDB = new MongoConfig();
	

	@Valid
	@NotNull
	@JsonProperty("jerseyClient")
	private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

	
	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClient;
	}

	public MongoConfig getCategoryMongoConfiguration() {
		// TODO Auto-generated method stub
		return this.categoryDB;
	}
	
	public MongoConfig getDataMongoConfiguration() {
		// TODO Auto-generated method stub
		return this.mongoDataDB;
	}
}
