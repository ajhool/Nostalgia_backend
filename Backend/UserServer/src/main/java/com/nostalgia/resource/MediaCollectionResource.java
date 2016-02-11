package com.nostalgia.resource;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.couchbase.client.java.document.JsonDocument;
import com.google.api.client.auth.openidconnect.IdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.nostalgia.ImageDownloaderBase64;
import com.nostalgia.MediaCollectionRepository;
import com.nostalgia.UserRepository;
import com.nostalgia.aws.SignedCookieCreator;
import com.nostalgia.client.SynchClient;
import com.nostalgia.exception.RegistrationException;
import com.nostalgia.persistence.model.LoginResponse;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.SyncSessionCreateResponse;
import com.nostalgia.persistence.model.User;

@Path("/api/v0/mediacollection")
public class MediaCollectionResource {

	@Context HttpServletResponse resp; 

	private static final Logger logger = LoggerFactory.getLogger(MediaCollectionResource.class);

	private final UserRepository userRepo;
	private final MediaCollectionRepository collRepo; 

	private SynchClient syncClient;


	public MediaCollectionResource( UserRepository userRepo, SynchClient syncClient, MediaCollectionRepository medCollRepo) {
		this.userRepo = userRepo;
		this.syncClient = syncClient;
		this.collRepo = medCollRepo;

	}

	
	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/new")
	@Timed
	public MediaCollection addCollection(MediaCollection creating, @QueryParam("creatorToken") String tok, @Context HttpServletRequest req) throws Exception{


		if(tok == null){
			throw new BadRequestException();
		}

		User creator = userRepo.findOneById(creating.getCreatorId());
		
		MediaCollection existing = collRepo.findOneById(creating.get_id());
		if(existing != null){
			throw new NotAllowedException("collection already exists");
		}

		//point user to new collection (subscription also happens in this step)
		creator.addCollection(creating);
	
		//update sync channels
		syncClient.setSyncChannels(creator);
		
		//save both user + collection
		userRepo.save(creator);
		collRepo.save(creating); 
		
		return creating;

	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/update")
	@Timed
	public MediaCollection updateCollection(MediaCollection updated, @QueryParam("updaterToken") String updaterTok, @Context HttpServletRequest req) throws Exception{


		collRepo.save(updated); 
		
		return updated; 

	}
	
	@SuppressWarnings("unused")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/delete")
	@Timed
	public MediaCollection removeCollection(@QueryParam("removerToken") String removerTok, @QueryParam("idToDelete") String idToDel, @Context HttpServletRequest req) throws Exception{


		MediaCollection toRemove = collRepo.findOneById(idToDel);
		User remover = userRepo.findOneById(removerTok);
		//unsubscribe user
		remover.removeCollection(toRemove);
		
		//delete from repo
		collRepo.remove(toRemove);
		
		userRepo.save(remover);
		return toRemove; 

	}
	
	@SuppressWarnings("unused")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/id")
	@Timed
	public MediaCollection getCollection(@QueryParam("collID") String targetId, @Context HttpServletRequest req) throws Exception{


		MediaCollection found = collRepo.findOneById(targetId);
		
		return found; 

	}




}
