package com.nostalgia;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.ws.rs.client.Client;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nostalgia.aws.AWSConfig;
import com.nostalgia.aws.SignedCookieCreator;
import com.nostalgia.client.IconService;
import com.nostalgia.client.SynchClient;
import com.nostalgia.resource.FriendsResource;
import com.nostalgia.resource.LocationAdminResource;
import com.nostalgia.resource.LocationQueryResource;
import com.nostalgia.resource.LocationSubscriptionResource;
import com.nostalgia.resource.MediaCollectionResource;
import com.nostalgia.resource.UserLocationResource;
//import com.nostalgia.resource.LocationResource;
import com.nostalgia.resource.UserResource;
import com.nostalgia.resource.VideoResource;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;



public class UserServerApp extends Application<UserAppConfig>{

	public static final String NAME = "Nostalgia";
	final static Logger logger = LoggerFactory
			.getLogger(UserServerApp.class);


	public static void main(String[] args) throws Exception {
		new UserServerApp().run(args);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void initialize(Bootstrap<UserAppConfig> bootstrap) {
		//bootstrap.addBundle(new AssetsBundle(, ));
	}

	private void configureCors(Environment environment) {
		Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
		filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		filter.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
		filter.setInitParameter("allowCredentials", "true");
	}

	private UserRepository getUserRepo(UserAppConfig config, Environment environment){
		UserRepository repo = new UserRepository(config.getUserServerConfig());

		return repo;
	}

	private LocationRepository getLocationRepo(UserAppConfig config, Environment environment){
		LocationRepository repo = new LocationRepository(config.getLocationServerConfig());

		return repo;
	}
	
	private MediaCollectionRepository getCollectionRepo(UserAppConfig config, Environment environment){
		MediaCollectionRepository repo = new MediaCollectionRepository(config.getCollectionServerConfig());

		return repo;
	}


	public IconService getIconService(UserAppConfig config, Environment environment){
		logger.info("creating icon server client...");
		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("Icon Client");

		IconService icSvc = new IconService(config.getIconServiceConfig(), jClient);

		return icSvc;
	}
	
	public SynchClient createSynchClient(UserAppConfig config, Environment environment){
		logger.info("creating synch server client...");
		final Client jClient = new JerseyClientBuilder(environment).using(
				config.getJerseyClientConfiguration()).build("sync communicator");
		SynchClient comms = new SynchClient(config.getSyncConfig(), jClient);

		return comms;
	}

	@Override
	public void run(UserAppConfig config, Environment environment) throws Exception {
		configureCors(environment);

		UserRepository userRepo = this.getUserRepo(config, environment);
		LocationRepository locRepo = this.getLocationRepo(config, environment);
		VideoRepository vidRepo = this.getVideoRepository(config, environment);
		MediaCollectionRepository collRepo =this.getCollectionRepo(config, environment);
	
		SynchClient sCli = this.createSynchClient(config, environment);
		IconService icSvc = this.getIconService(config, environment);
		SignedCookieCreator create = new SignedCookieCreator(new AWSConfig());
		
		UserLocationResource locRes = new UserLocationResource(userRepo, locRepo, vidRepo, sCli, collRepo);
		UserResource userResource = new UserResource(userRepo, sCli, locRes, icSvc, create, collRepo);
		VideoResource vidRes = new VideoResource(userRepo, vidRepo, locRepo, collRepo);
		LocationAdminResource locCRUD = new LocationAdminResource(  userRepo, locRepo, vidRepo, collRepo);
		LocationQueryResource queryRes = new LocationQueryResource(locRepo);
		LocationSubscriptionResource locSubRes = new LocationSubscriptionResource(userRepo, locRepo, sCli);
		FriendsResource friendRes = new FriendsResource(userRepo, sCli);
		MediaCollectionResource collRes = new MediaCollectionResource(userRepo, sCli, collRepo);

		environment.jersey().register(collRes);
		environment.jersey().register(friendRes);
		environment.jersey().register(locSubRes); 
		environment.jersey().register(queryRes);
		environment.jersey().register(locCRUD);
		environment.jersey().register(vidRes);
		environment.jersey().register(locRes);
		environment.jersey().register(userResource);

	}

	private VideoRepository getVideoRepository(UserAppConfig config, Environment environment) {
		VideoRepository repo = new VideoRepository(config.getVideoCouchConfig());
		return repo;
	}

}
