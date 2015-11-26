package com.nostalgia.resource;

import com.nostalgia.*;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Hex;
import org.geojson.Point;
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
import com.nostalgia.LocationRepository;
import com.nostalgia.UserRepository;
import com.nostalgia.VideoRepository;
import com.nostalgia.client.SynchClient;
import com.nostalgia.persistence.model.*;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Reading;
import facebook4j.conf.Configuration;
import facebook4j.conf.ConfigurationBuilder;

@Path("/api/v0/user/subscribe")
public class LocationSubscriptionResource {


	@Context HttpServletResponse resp; 

	private static final Logger logger = LoggerFactory.getLogger(LocationSubscriptionResource.class);

	private final UserRepository userRepo;
	private final LocationRepository locRepo;
	private final SynchClient sync;

	public User subscribeToLocation(User wantsSubscription, KnownLocation toSubscribeTo){

		wantsSubscription.subscribeToLocation(toSubscribeTo.get_id());

		sync.setSyncChannels(wantsSubscription);

		userRepo.save(wantsSubscription);
		return wantsSubscription;
	}


	public LocationSubscriptionResource( UserRepository userRepo, LocationRepository locRepo, SynchClient sync) {
		this.userRepo = userRepo;
		this.locRepo = locRepo;
		this.sync = sync;
		//this.sManager = manager;

	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/add")
	@Timed
	public User newLocation(@QueryParam("userId") String userId,  @QueryParam("locationId") String locationId,@Context HttpServletRequest req) throws Exception{

		if(locationId== null){
			throw new BadRequestException("no location specified to add");

		}

		if(userId == null){
			throw new BadRequestException("user id required");
		}

	
		User adding = userRepo.findOneById(userId);
		
		if(adding == null){
			throw new NotFoundException("no user found for id");
		}
		
		KnownLocation loc = locRepo.findOneById(locationId);
		if(loc == null){
			throw new NotFoundException("no location found for id: " + locationId);
		}
		
		User subscribed = subscribeToLocation(adding, loc);
		return adding;

	}



}
