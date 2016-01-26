package com.nostalgia.resource;

import com.fasterxml.jackson.databind.ObjectMapper; 
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilderException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.ClientConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
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
import com.nostalgia.LocationRepository;
import com.nostalgia.MediaCollectionRepository;
import com.nostalgia.UserRepository;
import com.nostalgia.VideoRepository;
import com.nostalgia.client.SynchClient;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.LoginResponse;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.SyncSessionCreateResponse;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Reading;
import facebook4j.conf.Configuration;
import facebook4j.conf.ConfigurationBuilder;

@Path("/api/v0/video")
public class VideoResource {


	@Context HttpServletResponse resp; 

	private static final Logger logger = LoggerFactory.getLogger(VideoResource.class);

	private static final String FileDataWorkingDirectory = "/webroot/data";

	private final VideoRepository vidRepo;
	private final UserRepository userRepo;
	private final LocationRepository locRepo;

	private static final ObjectMapper om = new ObjectMapper();
	private SynchClient syncClient;
	private final MediaCollectionRepository collRepo; 


	public VideoResource( UserRepository userRepo, VideoRepository vidRepo, LocationRepository locRepo, MediaCollectionRepository collRepo) {
		this.userRepo = userRepo;
		this.vidRepo = vidRepo;
		this.locRepo = locRepo;
		this.collRepo = collRepo;

	}

	//part 1, metadata is uploaded, in return for a video upload key
	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/new")
	@Timed
	public String addVideoMeta(Video adding, @QueryParam("auto") String auto, @Context HttpServletRequest req) throws Exception{

		//Video adding = om.readValue(addingString, Video.class);
		if(adding == null){
			throw new BadRequestException();
		}

		boolean autoAdd = true;

		try {
			autoAdd = Boolean.parseBoolean(auto);
		} catch (Exception e){
			logger.error("error getting auto add boolean, defaulting to auto enabled...", e);
		}

		if(adding.getLocation() == null){
			throw new BadRequestException("location is required");
		}

		User uploader = userRepo.findOneById(adding.getOwnerId());

		if(uploader == null){
			resp.sendError(404, "invalid uploader id specified for video");
			logger.error("no user found for user trying to upload video: " + adding.get_id());
			return null;
		}

		//set pointer to video on the user, subscribe them to it as well
		if(adding.getProperties() == null || adding.getProperties().get("sharing_who") == null){
			throw new BadRequestException("invalid sharing settings");
		}

		String sharing = adding.getProperties().get("sharing_who");
		//find any locations that this video maps to, and add it 

		HashMap<String, KnownLocation> matchingLocs = new HashMap<String, KnownLocation>();
		if(autoAdd){
			matchingLocs = locRepo.findKnownLocationsCoveringPoint(adding.getLocation());
		}

		//add in user specified locations 
		if(adding.getLocations() != null){
			for(String locId : adding.getLocations()){
				String channel = locId.substring(0, 8);
				if(!matchingLocs.containsKey(channel)){

					KnownLocation userSpecified = locRepo.findOneById(locId);
					matchingLocs.put(channel, userSpecified);
				}
			}
		} else {
			adding.setLocations(new ArrayList<String>());
		}

		adding.getLocations().clear();

		for(KnownLocation locToAdd : matchingLocs.values()){
			adding.getLocations().add(locToAdd.get_id());
		}


		switch(sharing){

		case(Video.WHO_EVERYONE): {
			String pubVidCollId = uploader.getPublicVideoCollId();
			MediaCollection publics = null;
			if(pubVidCollId == null){
				publics = new MediaCollection();
				publics.setName(uploader.get_id() + "_pub");
				publics.setCreatorId(uploader.get_id());
				publics.setVisibility(MediaCollection.PUBLIC);

				uploader.addCollection(publics);

			} else {
				publics =  collRepo.findOneById(pubVidCollId); 
			}

			publics.getMatchingVideos().put(adding.get_id(), Long.toString(System.currentTimeMillis()));
			collRepo.save(publics);

			//for public videos, maintain a reference in the location 
			if(matchingLocs != null && matchingLocs.size() > 0){
				for(KnownLocation loc : matchingLocs.values()){
					if(loc.getMatchingVideos() == null){
						loc.setMatchingVideos(new HashMap<String, String>());
					}

					int currentMax = loc.getMatchingVideos().size() - 1;

					currentMax++;
					loc.getMatchingVideos().put(currentMax + "", adding.get_id());
					JsonDocument saved = locRepo.save(loc);

				}
			} else {
				//set special null location for video
				matchingLocs = new HashMap<String, KnownLocation>();
				KnownLocation nullLoc = new KnownLocation();
				nullLoc.set_id("null_location");
				matchingLocs.put("null_location", nullLoc);
			}

			for(KnownLocation loc : matchingLocs.values()){
				String existingLocationCollId = uploader.findCollection(MediaCollection.PRIVATE,uploader.get_id() + ":" + loc.get_id()); 
				//if null, we have no videos here previously
				MediaCollection byLocation = null; 
				if(existingLocationCollId  == null){
					//create new media collection for this location for this user
					byLocation = new MediaCollection();
					byLocation.setName(uploader.get_id() + ":" + loc.get_id());
					byLocation.setCreatorId(uploader.get_id());
					byLocation.setVisibility(MediaCollection.PRIVATE);

					uploader.addCollection(byLocation);
				} else {
					byLocation = collRepo.findOneById(existingLocationCollId); 
				}



				if(byLocation.getMatchingVideos().keySet().contains(adding.get_id())){
					//then this video is already mapped to the location

				} else {
					byLocation.getMatchingVideos().put(adding.get_id(), Long.toString(System.currentTimeMillis())); 
				}
				collRepo.save(byLocation);
			}
			break;
		}
		case(Video.WHO_FRIENDS): {
			String sharedVidCollId = uploader.getSharedVideoCollId();
			MediaCollection shareds = null;
			if(sharedVidCollId == null){
				shareds = new MediaCollection();
				shareds.setName(uploader.get_id() + "_shared");
				shareds.setCreatorId(uploader.get_id());
				shareds.setVisibility(MediaCollection.SHARED);

				uploader.addCollection(shareds);
			} else {
				shareds =  collRepo.findOneById(sharedVidCollId); 
			}

			shareds.getMatchingVideos().put(adding.get_id(), Long.toString(System.currentTimeMillis()));
			collRepo.save(shareds);

			//for public videos, maintain a reference in the location 
			if(matchingLocs == null){
				//set special null location for video
				matchingLocs = new HashMap<String, KnownLocation>();
				KnownLocation nullLoc = new KnownLocation();
				nullLoc.set_id("null_location");
				matchingLocs.put("null_location", nullLoc);
			}

			for(KnownLocation loc : matchingLocs.values()){
				String existingLocationCollId = uploader.findCollection(MediaCollection.SHARED, uploader.get_id() + ":" + loc.get_id()); 
				//if null, we have no videos here previously
				MediaCollection byLocation = null; 
				if(existingLocationCollId  == null){
					//create new media collection for this location for this user
					byLocation = new MediaCollection();
					byLocation.setName(uploader.get_id() + ":" + loc.get_id());
					byLocation.setCreatorId(uploader.get_id());
					byLocation.setVisibility(MediaCollection.SHARED);

					uploader.addCollection(byLocation);
				} else {
					byLocation = collRepo.findOneById(existingLocationCollId); 
				}



				if(byLocation.getMatchingVideos().keySet().contains(adding.get_id())){
					//then this video is already mapped to the location

				} else {
					byLocation.getMatchingVideos().put(adding.get_id(), Long.toString(System.currentTimeMillis())); 
				}
				collRepo.save(byLocation);
			}
			break;
		}
		default:
		case(Video.WHO_PRIVATE):{
			String privateVidCollId = uploader.getPrivateVideoCollId();
			MediaCollection privates = null;
			if(privateVidCollId == null){
				privates = new MediaCollection();
				privates.setName(uploader.get_id() + "_priv");
				privates.setCreatorId(uploader.get_id());
				privates.setVisibility(MediaCollection.PRIVATE);

				uploader.addCollection(privates);
			} else {
				privates =  collRepo.findOneById(privateVidCollId); 
			}

			privates.getMatchingVideos().put(adding.get_id(), Long.toString(System.currentTimeMillis()));
			collRepo.save(privates);

			//for public videos, maintain a reference in the location 
			if(matchingLocs == null){
				//set special null location for video
				matchingLocs = new HashMap<String, KnownLocation>();
				KnownLocation nullLoc = new KnownLocation();
				nullLoc.set_id("null_location");
				matchingLocs.put("null_location", nullLoc);
			}

			for(KnownLocation loc : matchingLocs.values()){
				String existingLocationCollId = uploader.findCollection(MediaCollection.PRIVATE, uploader.get_id() + ":" + loc.get_id()); 
				//if null, we have no videos here previously
				MediaCollection byLocation = null; 
				if(existingLocationCollId  == null){
					//create new media collection for this location for this user
					byLocation = new MediaCollection();
					byLocation.setName(uploader.get_id() + ":" + loc.get_id());
					byLocation.setCreatorId(uploader.get_id());
					byLocation.setVisibility(MediaCollection.PRIVATE);

					uploader.addCollection(byLocation);
				} else {
					byLocation = collRepo.findOneById(existingLocationCollId); 
				}

				if(byLocation.getMatchingVideos().keySet().contains(adding.get_id())){
					//then this video is already mapped to the location

				} else {
					byLocation.getMatchingVideos().put(adding.get_id(), Long.toString(System.currentTimeMillis())); 
				}
				collRepo.save(byLocation);
			}
			break;
		}
		}

		//subscribe the user to their video
		uploader.subscribeToUserChannel(adding.getChannelName());

		userRepo.save(uploader);

		adding.setDateCreated(System.currentTimeMillis());
		adding.setStatus("METAONLY");
		JsonDocument saved = vidRepo.save(adding);
		return adding.get_id();
	}



	//step 2 is to upload 
	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes("*/*")
	@Path("/data")
	@Timed
	public Response uploadVideoData(final InputStream fileInputStream,
			@Context HttpServletRequest a_request,
			@QueryParam("vidId") String contentKey,
			@QueryParam("checksum") String checksum) throws Exception{

		Video matching = vidRepo.findOneById(contentKey);


		//Save video
		if(matching == null){
			return Response.status(404).entity("no metadata found for uploaded file ID").build();
		}

		if(checksum == null || checksum.equalsIgnoreCase("")){
			return Response.status(412).entity("Must provide MD5 for uploaded file").build();
		}



		File dataDir = new File(FileDataWorkingDirectory + "/" + matching.get_id() );
		dataDir.mkdirs(); 



		String savedFilePath = dataDir + "/" + matching.get_id();
		File original = new File(savedFilePath);



		boolean isMatch = false;
		String savedmd5 = null;
		if(original.exists()){
			//check MD5
			savedmd5 = this.md5Of(original);
			//if they match, then skip the upload process
			if(savedmd5.equalsIgnoreCase(checksum)){
				isMatch = true;
				fileInputStream.close();
				logger.info("exact checksum match found, skipping upload");
			}
		} 

		if(!isMatch){
			//wipe out whatever was there
			if(original.exists()){
				FileUtils.forceDelete(original);
			}

			//re-upload it
			long start = System.currentTimeMillis();
			original = saveFile(fileInputStream, savedFilePath);
			long end = System.currentTimeMillis();

			Duration thisRun = Duration.ofMillis(end -start);
			long speed = - 1;
			try {
				speed = (original.length() * 8) / (((end - start) / 1000) * (long)Math.pow(2, 20));
			} catch (Exception e){
				logger.error("errror computing save time", e);
			}
			logger.info("File: " + original.getName() + " took " + thisRun.toString() + " to download at a speed of " + speed + "mbps");
		}




		if(original == null || !original.exists()){
			logger.error("no file saved!");
			return Response.status(500).build();
		}


		//otherwise, assume file exists and is legit

		savedmd5 = md5Of(original);

		if(!savedmd5.equalsIgnoreCase(checksum)){
			//MD5 failure
			logger.error("file upload failed MD5 verification");
			FileUtils.forceDelete(original);
			return Response.status(400).build();
		}


		//store relevant data location info in video object


		//TODO



		matching.setStatus("METAANDDATA");

		vidRepo.save(matching);
		return Response.ok("Upload successful").build();

	}


	private synchronized static String md5Of(File saved) throws IOException{
		//verify with MD5


		FileInputStream fis = new FileInputStream(saved);
		String savedmd5 = DigestUtils.md5Hex(fis);
		fis.close();

		return savedmd5;

	}



	private File saveFile(InputStream uploadedInputStream,
			String serverLocation) {
		File target = new File(serverLocation);
		try {
			OutputStream outputStream = new FileOutputStream(new File(serverLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			logger.error("error saving uploaded file to disk", e);
			return null;
		}

		return target;
	}  
}
