package com.nostalgia.webserver.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;


public class WebServerGeneralConfig extends Configuration{
	    @NotNull
	    public String webRoot;

	 
}
