package com.nostalgia;

import java.util.EnumSet;

import javax.ws.rs.client.Client;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nostalgia.client.SynchClient;


public class ContentProcessorApp {

	public static final String NAME = "Nostalgia";
	final static Logger logger = LoggerFactory
			.getLogger(ContentProcessorApp.class);


	public static void main(String[] args) throws Exception {
		new ContentProcessorApp().run(args);
	}


	public String getName() {
		return NAME;
	}

	private UserRepository getUserRepo(ProcessorConfig config){
		UserRepository repo = new UserRepository(config.getUserCouchConfig());

		return repo;
	}

	private LocationRepository getLocationRepo(ProcessorConfig config){
		LocationRepository repo = new LocationRepository(config.getLocationCouchConfig());

		return repo;
	}

	
	public SynchClient createSynchClient(ProcessorConfig config){
		logger.info("creating synch server client...");
		final Client jClient = new JerseyClientBuilder().build();
		SynchClient comms = new SynchClient(config.getSyncConfig(), jClient);

		return comms;
	}

	private VideoRepository getVideoRepository(ProcessorConfig config) {
		VideoRepository repo = new VideoRepository(config.getVideoCouchConfig());
		return repo;
	}
	
	public void run(String[] args) throws Exception {

		ProcessorConfig config = new ProcessorConfig();

		UserRepository userRepo = this.getUserRepo(config);
		LocationRepository locRepo = this.getLocationRepo(config);
		VideoRepository vidRepo = this.getVideoRepository(config);
		SynchClient sCli = this.createSynchClient(config);

	}

}
