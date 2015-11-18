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

@Path("/api/v0/admin/location")
public class LocationAdminResource {


	@Context HttpServletResponse resp; 

	private static final Logger logger = LoggerFactory.getLogger(LocationAdminResource.class);

	private final UserRepository userRepo;
	private final LocationRepository locRepo;

	private VideoRepository vidRepo;



	public LocationAdminResource( UserRepository userRepo, LocationRepository locRepo, VideoRepository vidRepo) {
		this.userRepo = userRepo;
		this.locRepo = locRepo;
		this.vidRepo = vidRepo; 
		//this.sManager = manager;

	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/new")
	@Timed
	public KnownLocation newLocation(KnownLocation toAdd, @Context HttpServletRequest req) throws Exception{

		if(toAdd == null){
			throw new BadRequestException("no location specified to add");

		}

		if(toAdd.getLocation() == null){
			throw new BadRequestException("coordinates of some type are required to add a location");
		}

		if(toAdd.getCreatorId() == null || toAdd.getCreatorId().length() < 2){
			throw new BadRequestException("ID of creator is required");
		}

		if(toAdd.getProperties() == null){
			toAdd.setProperties(new HashMap<String, String>());
		}

		toAdd.getProperties().put("date_uploaded", (new Date(System.currentTimeMillis())).toString());


		//check for existence of this location
		//allow overlap of points

		HashMap<String, KnownLocation> result = locRepo.findKnownLocationsCoveringArea(toAdd.getLocation());

		if(result != null){
			for(KnownLocation existing : result.values()){
				if(toAdd.getName().equalsIgnoreCase(existing.getName()) || toAdd.get_id().equals(existing.get_id())){
					//then we have a dupliate
					resp.sendError(304, "location already exists, no changes made");
					return null;
				}
			}
		}


		//if location doesn't exist, find videos that fall within it
		HashMap<String, Video> matchingVids = vidRepo.findVideosWithin(toAdd.getLocation().getGeometry());

		if(toAdd.getMatchingVideos() == null){
			toAdd.setMatchingVideos(new HashMap<String, String>());
		}

		int currentMax = toAdd.getMatchingVideos().size() - 1;

		//add them in
		for(Video vid : matchingVids.values()){
			currentMax++;
			toAdd.getMatchingVideos().put(currentMax + "", vid.get_id());
		}

		//save
		JsonDocument saved = locRepo.save(toAdd);


		return LocationRepository.docToLocation(saved);

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/update")
	@Timed
	public KnownLocation updateLocation(KnownLocation toUpdate, @Context HttpServletRequest req) throws Exception{
		if(toUpdate == null){
			throw new BadRequestException("no location specified to update");

		}

		if(toUpdate.getLocation() == null){
			throw new BadRequestException("coordinates of some type are required to update a location");
		}

		if(toUpdate.getProperties() == null){
			toUpdate.setProperties(new HashMap<String, String>());
		}

		toUpdate.getProperties().put("date_updated", (new Date(System.currentTimeMillis())).toString());

		//if location doesn't already exist, throw error

		KnownLocation existing = locRepo.findOneById(toUpdate.get_id());
		if(existing == null){
			resp.sendError(404, "must update an existing document");
			return null;
		}

		//otherwise, do simple upsert for now
		JsonDocument saved = locRepo.save(toUpdate);


		return LocationRepository.docToLocation(saved);
	}


	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/delete")
	@Timed
	public KnownLocation deleteLocation(KnownLocation toDelete, @Context HttpServletRequest req) throws Exception{
		if(toDelete == null){
			throw new BadRequestException("no location specified to delete");

		}

		//if location doesnt already exist, throw error
		KnownLocation existing = locRepo.findOneById(toDelete.get_id());
		if(existing == null){
			resp.sendError(404, "must delete an existing document");
			return null;
		}
		//otherwise, do delete based on id

		JsonDocument removed = locRepo.remove(toDelete);

		return LocationRepository.docToLocation(removed);
	}

}
