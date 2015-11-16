package com.portol.contentserver.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;


public class PaymentGetterConfig extends Configuration{
	    @NotNull
	    public String host;

	    @Min(1)
	    @Max(65535)
	    public int port;
	    
	    public String path;

	    public String user;

	    public String password;
	    
	    @NotNull
	    public String apiKey;
	    
	    public String uniquePath;
	
}
