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
import com.nostalgia.client.IconService;
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

@Path("/api/v0/user")
public class UserResource {

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

	private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

	private final UserRepository userRepo;

	private SynchClient syncClient;
	private UserLocationResource userLocRes;
	private final IconService icSvc; 
	private final SignedCookieCreator creator; 


	public UserResource( UserRepository userRepo, SynchClient syncClient, UserLocationResource userLoc, IconService icSvc, SignedCookieCreator create) {
		this.userRepo = userRepo;
		this.syncClient = syncClient;
		this.userLocRes = userLoc; 
		this.icSvc = icSvc;
		this.creator = create;
	}

	private void setNewStreamingTokens(User needsTokens, long tokenExpiryDate) throws Exception{
		if(needsTokens.getStreamTokens() == null){
			needsTokens.setStreamTokens(new HashMap<String, String>());
		}

		//call to aws here if needed for new tokens
		Map<String, String> generated = creator.generateCookies("https://d1natzk16yc4os.cloudfront.net/*", tokenExpiryDate);
		
		needsTokens.getStreamTokens().putAll(generated);
		
		return;
	}
	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/login")
	@Timed
	public LoginResponse userLogin(User loggingIn, @QueryParam("type") String type, @Context HttpServletRequest req) throws Exception{


		if(loggingIn == null){
			throw new BadRequestException();
		}

		LoginResponse response = new LoginResponse();
		//TODO move to service
		if(false /*!mpdReq.getApiKey().equalsIgnoreCase("foo")*/){
			throw new ForbiddenException();
		}

		User loggedIn; 
		if(type == null || type.equalsIgnoreCase("app")){
			//lookup via uname/pass
			loggedIn = loginWithPass(loggingIn);

		} else {
			//lookup via token

			switch(type){
			case("facebook"):
				loggedIn = loginWithFacebook(loggingIn);
			break;
			case("google"):
				loggedIn = loginWithGoogle(loggingIn);
			break;
			default:
				logger.error("unable to infer type to login");
				resp.sendError(400, "must specify login type!");
				return null;
			}
		}

		if(loggedIn == null){
			resp.sendError(404, "no user found");
			return null;
		}

		//open session for user's mobile db
		SyncSessionCreateResponse syncResp = syncClient.createSyncSessionFor(loggedIn);

		if(syncResp == null){
			//register user and try again
			syncClient.registerUser(loggedIn); 
			syncResp = syncClient.createSyncSessionFor(loggedIn);
			if(syncResp == null){
				throw new Exception("sync registration failed!");

			}
		}

		loggedIn.setSyncToken(syncResp.getSession_id());

		if(loggedIn.getAuthorizedDevices() == null){
			loggedIn.setAuthorizedDevices(new ArrayList<String>());
			loggedIn.getAuthorizedDevices().addAll(loggingIn.getAuthorizedDevices());
		} else {

			//merge in the device ID
			for(int i = 0; i < loggingIn.getAuthorizedDevices().size(); i++){
				boolean exists = false;
				for(int j = 0; j < loggedIn.getAuthorizedDevices().size(); j ++){
					if(loggingIn.getAuthorizedDevices().get(i).equalsIgnoreCase(loggedIn.getAuthorizedDevices().get(j))){
						exists = true;
						break;
					}
				}

				if(!exists){
					loggedIn.getAuthorizedDevices().add(loggingIn.getAuthorizedDevices().get(i));
				}
			}


		}

		loggedIn.setLastSeen(System.currentTimeMillis());

		response.setSessionTok(syncResp.getSession_id());
		
	
		long time = 1451066974000L; 
		
//		//refresh tokens if necessary
//		if(loggedIn.getStreamTokens() != null){
//			long expiry = Long.parseLong(loggedIn.getStreamTokens().get("CloudFront-Expires"));
//			if(expiry < System.currentTimeMillis()){
//				//need new set of tokens
//				this.setNewStreamingTokens(loggedIn, System.currentTimeMillis() + MONTH_IN_MILLIS);
//			}
//			
//		} else {
//			this.setNewStreamingTokens(loggedIn, System.currentTimeMillis() + MONTH_IN_MILLIS);
//		}
		this.setNewStreamingTokens(loggedIn, System.currentTimeMillis() + MONTH_IN_MILLIS);
		if(loggingIn.getLastKnownLoc() != null){
			loggedIn.setLastKnownLoc(loggingIn.getLastKnownLoc());
			loggedIn = userLocRes.updateSubscriptions(loggedIn);
		}

		userRepo.save(loggedIn);
		return response;

	}


	private static final String CLIENT_ID = "455723277988-mdc4rhk2nitc31slqdrhhgdlv0u6m3vk.apps.googleusercontent.com";
	/**
	 * Default JSON factory to use to deserialize JSON.
	 */
	private final JacksonFactory JSON_FACTORY = new JacksonFactory();


	/**
	 * Default HTTP transport to use to make HTTP requests.
	 */
	private HttpTransport TRANSPORT;

	private User loginWithGoogle(User loggingIn) throws Exception {
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(TRANSPORT, JSON_FACTORY).setAudience(Arrays.asList(CLIENT_ID)).build();

		// (Receive idTokenString by HTTPS POST)
		GoogleIdToken idToken = verifier.verify(loggingIn.getToken());

		User result = null;
		if (idToken != null) {
			Payload payload = idToken.getPayload();

			//check for existence of token in db 
			try {
				result = userRepo.findOneByAccountToken(payload.getSubject(), "google");
			} catch (Exception e) {

				logger.error("error finding user with token login", e);
				resp.sendError(503, "database error");
				return null;
			}



		} else {
			logger.error("Invalid ID token.");

		}


		return result; 
	}



	public static final String FB_APP_ID = "1080777671932760";
	public static final String FB_APP_SECRET = "d2711fde8327b4a1e7db55dd649f6315";



	private User loginWithFacebook(User loggingIn) throws IllegalStateException, FacebookException {
		User result = null;
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setDebugEnabled(true);
		configurationBuilder.setOAuthAppId(FB_APP_ID);
		configurationBuilder.setOAuthAppSecret(FB_APP_SECRET);
		configurationBuilder.setAppSecretProofEnabled(false);
		configurationBuilder.setOAuthAccessToken(loggingIn.getToken());
		configurationBuilder
		.setOAuthPermissions("email, id, name, first_name, last_name, gender, picture, verified, locale, generic");
		configurationBuilder.setUseSSL(true);
		configurationBuilder.setJSONStoreEnabled(true);

		// Create configuration and get Facebook instance
		Configuration configuration = configurationBuilder.build();
		FacebookFactory ff = new FacebookFactory(configuration);
		Facebook facebook = ff.getInstance();
		String name = facebook.getName();
		facebook4j.User me = facebook.getMe();

		//check for existence of token in db 
		try {
			result = userRepo.findOneByAccountToken(me.getId(), "facebook");
		} catch (Exception e) {
			logger.error("error finding facebook user with token: " + me.getId());
			return null;
		}

		if(result == null) return null;

		logger.info("logged in with facebook successfully");
		return result; 

	}

	private User loginWithPass(User loggingIn) {
		User result = null;
		List<User> withName = null;
		try {
			withName = userRepo.findByName(loggingIn.getName());
		} catch (Exception e) {
			logger.error("error finding user by name", e);
			e.printStackTrace();
			return null;
		}

		if(withName == null){
			return null;
		}

		//check pw
		for(User match:withName){
			if(match.getPassword().equalsIgnoreCase(loggingIn.getPassword())){
				//then we have a match
				result = match;
				break;
			}
		}


		return result;
	}



	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/register")
	@Timed
	public LoginResponse userRegister(User registering, @QueryParam("type") String type, @Context HttpServletRequest req) throws Exception{


		if(registering == null){
			throw new BadRequestException();
		}

		LoginResponse response = new LoginResponse();
		//TODO move to service
		if(false /*!mpdReq.getApiKey().equalsIgnoreCase("foo")*/){
			throw new ForbiddenException();
		}
		User existing = null;
		if(registering.getToken() != null){

			existing = userRepo.findOneByAccountToken(registering.getToken(), type);

		} else {

			existing = userRepo.findOneByEmail(registering);

		}

		if(existing != null){
			resp.sendError(503, "user already exists, please log in");
			return null;
		}

		//user always subscrbes to itself
		ArrayList<String> channels = new ArrayList<String>();
		String userChannel = registering.getChannelName();
		channels.add(userChannel);

		registering.setChannels(channels);

		JsonDocument loggedIn; 
		if(type == null || type.equalsIgnoreCase("app")){
			//lookup via uname/pass
			loggedIn = registerNewUserApp(registering);

		} else {
			//lookup via token

			switch(type){
			case("facebook"):
				try {
					loggedIn = registerNewUserFacebook(registering);
				} catch (RegistrationException e){
					logger.error("error registering user", e);
					resp.sendError(403, e.getMessage());
					return null;
				}
			break;
			case("google"):
				loggedIn = registerNewUserGoogle(registering);
			break;
			default:
				logger.error("unable to infer type to login");
				resp.sendError(400, "must specify login type!");
				return null;
			}
		}

		if(loggedIn == null){
			resp.sendError(404, "no user found");
			return null;
		}

		//open session for user's mobile db
		User loggedInUser = userRepo.docToUser(loggedIn);

		if(loggedInUser == null){
			throw new Exception("unable to parse user");
		}

//Set image
		String image = null;
		try {
			image = icSvc.getBase64Icon(loggedInUser.getName());
		} catch (Exception e){
			logger.error("error getting icon", e);
		}

		loggedInUser.setIcon(image);
		
		//set default settings
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("sharing_who", WHO_EVERYONE);
		settings.put("sharing_when", WHEN_WIFI);
		settings.put("sharing_where", WHERE_EVERYWHERE);
		settings.put("video_sound", SOUND_MUTE);
		this.setNewStreamingTokens(loggedInUser, System.currentTimeMillis() + MONTH_IN_MILLIS);
		loggedInUser.setSettings(settings);

		loggedInUser.setDateJoined(System.currentTimeMillis());
		loggedInUser.setLastSeen(System.currentTimeMillis());
		loggedInUser.setPasswordChangeDate(System.currentTimeMillis());
		loggedInUser.subscribeToUserChannel(userChannel);
		SyncSessionCreateResponse syncResp = syncClient.createSyncSessionFor(loggedInUser);

		if(syncResp == null){
			//register user and try again
			syncClient.registerUser(loggedInUser); 
			syncResp = syncClient.createSyncSessionFor(loggedInUser);
			if(syncResp == null){
				throw new Exception("sync registration failed!");

			}
		}

		loggedInUser.setSyncToken(syncResp.getSession_id());


		response.setSessionTok(syncResp.getSession_id());


		if(loggedInUser.getAuthorizedDevices() == null){
			loggedInUser.setAuthorizedDevices(new ArrayList<String>());
			loggedInUser.getAuthorizedDevices().addAll(registering.getAuthorizedDevices());
		} else {

			//merge in the device ID(s)
			for(int i = 0; i < registering.getAuthorizedDevices().size(); i++){
				boolean exists = false;
				for(int j = 0; j < loggedInUser.getAuthorizedDevices().size(); j ++){
					if(registering.getAuthorizedDevices().get(i).equalsIgnoreCase(loggedInUser.getAuthorizedDevices().get(j))){
						exists = true;
						break;
					}
				}

				if(!exists){
					loggedInUser.getAuthorizedDevices().add(registering.getAuthorizedDevices().get(i));
				}
			}


		}

		if(registering.getLastKnownLoc() != null){
			loggedInUser.setLastLocationUpdate(System.currentTimeMillis());
			loggedInUser.setLastKnownLoc(registering.getLastKnownLoc());
			loggedInUser = userLocRes.updateSubscriptions(loggedInUser);
		}

		userRepo.save(loggedInUser);

		return response;

	}

	private JsonDocument registerNewUserApp(User registering) throws Exception{
		return userRepo.save(registering);
	}

	private JsonDocument registerNewUserGoogle(User toRegister) throws Exception {
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(TRANSPORT, JSON_FACTORY).setAudience(Arrays.asList(CLIENT_ID)).build();

		// (Receive idTokenString by HTTPS POST)
		GoogleIdToken idToken = verifier.verify(toRegister.getToken());
		Payload payload = null;
		User result = null;
		if (idToken != null) {
			payload = idToken.getPayload();
		} else {
			logger.error("Invalid ID token.");

		}


		String email = "null@null.com";//payload.getEmail();
		String userName = email.substring(0, email.indexOf('@'));

		Map<String, Object> vals = payload.getUnknownKeys();
		String name = null;
		String firstName = null;
		String lastName = null;
		String locale = null;
		if(vals != null){
			name = vals.get("name").toString();
			firstName = vals.get("given_name").toString();
			lastName = vals.get("family_name").toString();
			locale = vals.get("locale").toString();
		}

		User added = new User();
		added.setEmail(email);
		added.setName(name.replaceAll("\\s+",""));
		//added.setUserName(userName);
		added.setPassword("null");

		//create google account
		added.getAccounts().put(payload.getSubject(), "google");
		JsonDocument regd = userRepo.save(added); 

		return regd; 
	}

	private JsonDocument registerNewUserFacebook(User user) throws Exception {

		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setDebugEnabled(true);
		configurationBuilder.setOAuthAppId(FB_APP_ID);
		configurationBuilder.setOAuthAppSecret(FB_APP_SECRET);
		configurationBuilder.setAppSecretProofEnabled(false);
		configurationBuilder.setOAuthAccessToken(user.getToken());
		configurationBuilder
		.setOAuthPermissions("email, id, name, first_name, last_name, gender, picture, verified, locale, generic");
		configurationBuilder.setUseSSL(true);
		configurationBuilder.setJSONStoreEnabled(true);

		// Create configuration and get Facebook instance
		Configuration configuration = configurationBuilder.build();
		FacebookFactory ff = new FacebookFactory(configuration);
		Facebook facebook = ff.getInstance();
		String name = facebook.getName();
		facebook4j.User me = facebook.getMe();

		me = facebook.getUser(me.getId(), new Reading().fields("email", "last_name", "gender", "first_name", "picture", "name", "locale", "verified"));

		//		if(me.getEmail() == null){
		//			resp.sendError(503, "unable to register account, email required");
		//			return null;
		//		}



		User vueUser = new User();

		String email = me.getEmail();
		String selfDeclaredEmail = user.getEmail();

		if(email == null && selfDeclaredEmail == null){
			throw new RegistrationException("Email Required to register");
		}

		String toSave = "";

		if(email != null){
			toSave = email;
		} else {
			toSave = selfDeclaredEmail; 
		}

		if(!toSave.contains("@") || !toSave.contains(".")){
			throw new RegistrationException("invalid email");
		}

		//		if(email != null) {
		vueUser.setEmail(toSave);
		vueUser.setName(toSave.substring(0, email.indexOf('@')));
		//		} else {
		//			if(name != null){
		//				vueUser.setName(name.replaceAll("\\s+",""));
		//			}
		//		}
		if(vueUser.getAccounts() == null){
			HashMap<String, String> map = new HashMap<String, String>();
			vueUser.setAccounts(map);
		}

		vueUser.getAccounts().put(me.getId(), "facebook");


		if(me.getPicture() != null && me.getPicture().getURL() != null){

			//we have a fb picture to use
			ImageDownloaderBase64 imgDL = new ImageDownloaderBase64(me.getPicture().getURL().toString());
			Thread dl = new Thread(imgDL);
			dl.start();
			try {
				dl.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			vueUser.setIcon(imgDL.getEncodedImage());

		}

		JsonDocument regd = userRepo.save(vueUser); 

		return regd; 
	}



}
