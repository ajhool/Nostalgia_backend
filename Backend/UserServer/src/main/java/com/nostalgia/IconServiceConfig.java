package com.nostalgia;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;

public class IconServiceConfig extends Configuration {

	@Valid
	@NotNull
	public String iconHost;
	
	@Valid
	@NotNull
	@Min(1)
	@Max(65535)
	public int port;

	@Valid
	@NotNull
	public String newIconPath;
	
	

}
