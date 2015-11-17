package com.nostalgia.webserver;

import java.io.File;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;

import org.apache.commons.io.Charsets;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nostalgia.webserver.auth.BasicUser;
import com.nostalgia.webserver.auth.SimpleAuthenticator;
import com.nostalgia.webserver.config.WebServerConfig;
import com.nostalgia.webserver.resource.WebServlet;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class WebServerApp extends Application<WebServerConfig> {

	public static final String NAME = "WebServerApp";
	final static Logger logger = LoggerFactory
			.getLogger(WebServerApp.class);

	public static void main(String[] args) throws Exception {
		new WebServerApp().run(args);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void initialize(Bootstrap<WebServerConfig> bootstrap) {
		super.initialize(bootstrap);
		//bootstrap.addBundle(new AssetsBundle("/assets", "/", "portolMimic.html"));
		//bootstrap.addBundle(websocket);
	}

	private void configureCors(Environment environment) {
		Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
		filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM,
				"GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		filter.setInitParameter(
				CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER,
				"Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Cookies");
		filter.setInitParameter("allowCredentials", "true");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
	}

	@Override
	public void run(WebServerConfig config, Environment environment)
			throws Exception {

		configureCors(environment);

		String webRootPath = config.getWsConfig().webRoot; 

//		File playerRoot = new File(webRoot, "player");
//		File faxRoot = new File(webRoot, "moviefax");
       // environment.jersey().register(AuthFactory.binder(new BasicAuthFactory<BasicUser>(new SimpleAuthenticator("portol", "portol"), "SECURITY REALM", BasicUser.class)));
        
		//IndexResource index = new IndexResource( webRoot, Charsets.UTF_8);
//		StudioResource stdio = new StudioResource( studioRoot, Charsets.UTF_8);
//		PlayerResource playerRes = new PlayerResource( playerRoot, Charsets.UTF_8);
		//environment.jersey().register(index);
//		environment.jersey().register(stdio);
//		environment.jersey().register(playerRes);
		
//		
//		BasicUser authd = new BasicUser("porto"
//				+ "l", "portol");
//		WebServlet playerServlet = new WebServlet("/webroot/player", "/player", "index.html", Charsets.UTF_8, authd);
//		environment.servlets().addServlet("player", playerServlet).addMapping("/player/*");
		
		WebServlet dataServlet = new WebServlet(webRootPath, "/data", Charsets.UTF_8);
		environment.servlets().addServlet("data", dataServlet).addMapping("/data/*");
		
	
	}

}
