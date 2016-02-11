package com.nostalgia.resource;


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

import com.codahale.metrics.annotation.Timed;
import com.couchbase.client.java.document.JsonDocument;
import com.google.api.client.auth.openidconnect.IdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.nostalgia.ImageDownloaderBase64;
import com.nostalgia.UserRepository;
import com.nostalgia.aws.SignedCookieCreator;
import com.nostalgia.client.SynchClient;
import com.nostalgia.exception.RegistrationException;
import com.nostalgia.persistence.model.LoginResponse;
import com.nostalgia.persistence.model.SyncSessionCreateResponse;
import com.nostalgia.persistence.model.User;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Reading;
import facebook4j.conf.Configuration;
import facebook4j.conf.ConfigurationBuilder;

@Path("/api/v0/friends")
public class FriendsResource {

	public static final String WHO_PRIVATE = "PRIVATE";
	public static final String WHO_FRIENDS = "FRIENDS";
	public static final String WHO_EVERYONE = "EVERYONE";
	public static final long MONTH_IN_MILLIS = 2592000000L;
	public static final String WHEN_NOW = "NOW";
	public static final String WHEN_HOUR = "HOUR";
	public static final String WHEN_DAY = "ONE_DAY";
	public static final String WHEN_WIFI = "WIFI";
	private static final String SOUND_MUTE = "MUTE";
	private static final String SOUND_ENABLED = "ENABLED";
	public static final String WHERE_HERE = "HERE";
	public static final String WHERE_EVERYWHERE = "EVERYWHERE";

	@Context HttpServletResponse resp; 

	private static final Logger logger = LoggerFactory.getLogger(FriendsResource.class);

	private final UserRepository userRepo;

	private SynchClient syncClient;


	public FriendsResource( UserRepository userRepo, SynchClient syncClient) {
		this.userRepo = userRepo;
		this.syncClient = syncClient;

	}

	
	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/add")
	@Timed
	public User addFriend(String userAddingFriendId, @QueryParam("friendId") String friendId, @Context HttpServletRequest req) throws Exception{


		if(userAddingFriendId == null){
			throw new BadRequestException();
		}

		User addingFriend = userRepo.findOneById(userAddingFriendId);
		User friendToAdd = userRepo.findOneById(friendId); 

		if(addingFriend.getFriends().keySet().contains(friendToAdd.get_id())){
			//we are already friends or pending
			return null;
		}
		
		//otherwise, we aren't friends, so add it in
		
		addingFriend.subscribeToFriend(friendToAdd);
		syncClient.setSyncChannels(addingFriend);
		userRepo.save(addingFriend);
		return addingFriend;

	}

	@SuppressWarnings("unused")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/search")
	@Timed
	public List<User> findFriend(@QueryParam("friendName") String friendName, @Context HttpServletRequest req) throws Exception{


		List<User> results = userRepo.searchByName(friendName);
		
		//scrub user info
		for(User cur : results){
			cur.setPassword(null);
			cur.setAccounts(null);
			cur.setAuthorizedDevices(null);
			cur.setEmail(null);
			cur.setToken(null);
			cur.setSettings(null);
			cur.setStreamTokens(null);
			cur.setToken(null);
		}
		
		return results; 

	}
	
	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/remove")
	@Timed
	public User removeFriend(String userRemovingFriendId, @QueryParam("friendId") String friendIdtoRemove, @Context HttpServletRequest req) throws Exception{


		if(userRemovingFriendId == null){
			throw new BadRequestException();
		}

		User removingFriend = userRepo.findOneById(userRemovingFriendId);
		User friendToRemove = userRepo.findOneById(friendIdtoRemove); 
		
		if(!removingFriend.getFriends().keySet().contains(friendToRemove.get_id())){
			//we dont have the friend we are trying to remove
			return null;
		}
		
		
		removingFriend.unsubscribeFromFriend(friendToRemove);
		syncClient.setSyncChannels(removingFriend);
		userRepo.save(removingFriend);
		return removingFriend; 

	}




}
