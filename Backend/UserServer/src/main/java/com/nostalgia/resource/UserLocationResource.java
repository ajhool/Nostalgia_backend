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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
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

import com.cocoahero.android.geojson.GeoJSONObject;
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
import com.nostalgia.persistence.model.LoginResponse;
import com.nostalgia.persistence.model.SyncSessionCreateResponse;
import com.nostalgia.persistence.model.*;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Reading;
import facebook4j.conf.Configuration;
import facebook4j.conf.ConfigurationBuilder;

@Path("/api/v0/user/location")
public class UserLocationResource {


	@Context HttpServletResponse resp; 
	
	private static final Logger logger = LoggerFactory.getLogger(UserLocationResource.class);

	private final UserRepository userRepo;
	private final LocationRepository locRepo;
	//private final SubscriptionManager sManager; 
	
	
	public UserLocationResource( UserRepository userRepo, LocationRepository locRepo/*, SubscriptionManager manager*/) {
		this.userRepo = userRepo;
		this.locRepo = locRepo;
		//this.sManager = manager;
		
	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public void userLocationUpdate(GeoJSONObject newLoc, @QueryParam("userId") String userId, @Context HttpServletRequest req) throws Exception{

		if(newLoc == null){
			throw new BadRequestException();
		}

		User matching = userRepo.findOneById(userId);
		if(matching == null) return;
		
		matching.setLastKnownLoc(newLoc);
		
		
		
		//all the locations we know
		HashMap<String, KnownLocation> nearbys = locRepo.findKnownLocationsCoveringPoint(newLoc);
		
		matching.updateLocationChannels(nearbys.keySet());
		
		
		userRepo.save(matching);
		
		return;

	}

}
