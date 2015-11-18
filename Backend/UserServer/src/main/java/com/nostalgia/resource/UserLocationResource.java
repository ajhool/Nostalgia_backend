package com.nostalgia.resource;


import java.util.HashMap;

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
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.nostalgia.LocationRepository;
import com.nostalgia.UserRepository;
import com.nostalgia.persistence.model.*;

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

	public User updateSubscriptions(User hasNewLoc){
		//all the locations we know
		HashMap<String, KnownLocation> nearbys = locRepo.findKnownLocationsCoveringPoint(hasNewLoc.getLastKnownLoc());

		if(nearbys != null && nearbys.keySet().size() > 0){
			hasNewLoc.updateLocationChannels(nearbys.keySet());
		}


		return hasNewLoc; 
	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/update")
	@Timed
	public void userLocationUpdate(Point newLoc, @QueryParam("userId") String userId, @Context HttpServletRequest req) throws Exception{

		if(newLoc == null){
			throw new BadRequestException();
		}

		User matching = userRepo.findOneById(userId);
		if(matching == null) return;

		matching.setLastKnownLoc(newLoc);
		matching.setLastLocationUpdate(System.currentTimeMillis());

		matching = this.updateSubscriptions(matching);

		userRepo.save(matching);

		return;

	}

}
