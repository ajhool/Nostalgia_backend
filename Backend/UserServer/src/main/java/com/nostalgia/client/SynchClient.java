package com.nostalgia.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nostalgia.SyncConfig;
import com.nostalgia.persistence.model.SyncSessionCreateRequest;
import com.nostalgia.persistence.model.SyncSessionCreateResponse;
import com.nostalgia.persistence.model.User;

public class SynchClient {
	private final SyncConfig conf;
	private final Client sComm; 
	private static final Logger logger = LoggerFactory.getLogger(SynchClient.class.getName().toLowerCase());

	public SynchClient(SyncConfig syncConfig, Client jClient) {
		conf = syncConfig;
		sComm = jClient;
	}



	public boolean registerUser(User loggedIn) {
		UriBuilder uribuild = UriBuilder.fromUri("http://" + conf.host + ":" + conf.port + conf.addUserPath);


		try {
			Response resp = sComm.target(uribuild).request().post(Entity.json(loggedIn));

			logger.info("response: " + resp.getStatus());
		} catch (Exception e){
			logger.error("error registering new user", e);
			return false;
		}


		return true;
	}

	public SyncSessionCreateResponse createSyncSessionFor(User loggedIn) {
		UriBuilder uribuild = UriBuilder.fromUri("http://" + conf.host + ":" + conf.port + conf.newSessionPath);

		SyncSessionCreateRequest req = new SyncSessionCreateRequest();
		req.setName(loggedIn.getName());
		SyncSessionCreateResponse resp = null;
		try {
		 resp = sComm.target(uribuild).request().post(Entity.json(req), SyncSessionCreateResponse.class);
		} catch (Exception e){
			logger.info("error creating sync session for user");
			return null;
		}
		return resp;
	}

}
