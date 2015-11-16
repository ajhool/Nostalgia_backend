package com.portol.contentserver.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;


public class MongoConfig extends Configuration{
	    @NotNull
	    public String host;

	    @Min(1)
	    @Max(65535)
	    public int port;

	    @NotNull
	    public String db;

	    @NotNull
	    public String user;

	    @NotNull
	    public String password;
	
}
