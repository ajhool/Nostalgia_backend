package com.nostalgia.resource;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.nostalgia.LocationRepository;
import com.nostalgia.MediaCollectionRepository;
import com.nostalgia.UserRepository;
import com.nostalgia.VideoRepository;
import com.nostalgia.client.AtomicOpsClient;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;

@Path("/api/v0/atomic")
public class AtomicOpsResource {

	private static final Logger logger = LoggerFactory.getLogger(AtomicOpsResource.class);
	private final AtomicOpsClient atomicCli;
	private final UserRepository userRepo;
	private final MediaCollectionRepository collRepo;
	private final VideoRepository vidRepo;
	private final LocationRepository locationRepo; 

	public AtomicOpsResource(UserRepository userRepo, AtomicOpsClient atomicCli, MediaCollectionRepository collRepo, VideoRepository vidRepo, LocationRepository locRepo) {
		this.userRepo = userRepo;
		this.atomicCli = atomicCli; 
		this.locationRepo = locRepo; 
		this.collRepo = collRepo;
		this.vidRepo = vidRepo; 
	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/favorite")
	@Timed
	public String favoriteVideo(String videoId, @QueryParam("userid")String userid) throws Exception{
		//save to user's saved collection 
		User adding = userRepo.findOneById(userid);

		if(adding == null){
			throw new NotFoundException("user not found");
		}

		//get video favorte counter
		Video hasCounter = vidRepo.findOneById(videoId);

		if(hasCounter == null){
			throw new NotFoundException("video not found");
		}

		String saved = adding.findCollection(MediaCollection.PRIVATE, "Saved");
		MediaCollection savedColl = collRepo.findOneById(saved);

		savedColl.getMatchingVideos().put(videoId, Long.toString(System.currentTimeMillis())); 
		collRepo.save(savedColl);



		//increment counter on video
		atomicCli.incrementCounter(hasCounter.getFavoriteCounterId());
		return videoId; 

	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/flag")
	@Timed
	public String flagVideo(String videoId, @QueryParam("userid")String userid){
		//add report

		Video hasCounter = vidRepo.findOneById(videoId);
		if(hasCounter == null){
			throw new NotFoundException("video not found");
		}

		atomicCli.addPrependedItem(hasCounter.getFlagTrackerId(), userid, System.currentTimeMillis());
		return videoId; 
	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/report/skipped")
	@Timed
	public String skippedVideo(String videoId, @QueryParam("userid")String userid){

		//increment counter
		Video hasCounter = vidRepo.findOneById(videoId);
		if(hasCounter == null){
			throw new NotFoundException("video not found");
		}
		atomicCli.incrementCounter(hasCounter.getSkipCounterId());
		return videoId; 
	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/report/viewed")
	@Timed
	public String viewedVideo(String videoId, @QueryParam("userid")String userid) throws Exception{
		//add viewed to user object
		//save to user's viewed set
		User adding = userRepo.findOneById(userid);

		if(adding == null){
			throw new NotFoundException("user not found");
		}


		//get video viewed counter
		Video hasCounter = vidRepo.findOneById(videoId);

		if(hasCounter == null){
			throw new NotFoundException("video not found");
		}
		adding.addSeenVideo(videoId);

		//increment counter
		atomicCli.incrementCounter(hasCounter.getViewCounterId()); 
		return videoId; 
	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/upvote")
	@Timed
	public String upvote(String idOfObjectToUpvote, @QueryParam("type") String type, @QueryParam("userid")String userid) throws Exception{
		String idOfTracker = null;
		switch(type){
		case("USER"):{
			User toUpvote = userRepo.findOneById(idOfObjectToUpvote);
			if(toUpvote == null){
				throw new NotFoundException("user not found");
			}
			idOfTracker = toUpvote.getUpvoteTrackerId(); 
			break;
		}
		case("LOCATION"):{
			KnownLocation toUpvote = locationRepo.findOneById(idOfObjectToUpvote);
			if(toUpvote == null){
				throw new NotFoundException("location not found");
			}
			idOfTracker = toUpvote.getUpvoteTrackerId(); 
			break;
		}
		case("VIDEO"):
		{
			Video toUpvote = vidRepo.findOneById(idOfObjectToUpvote);
			if(toUpvote == null){
				throw new NotFoundException("video not found");
			}
			idOfTracker = toUpvote.getUpvoteTrackerId(); 
			break;
		}

		case("COLLECTION"):
		{
			MediaCollection toUpvote = collRepo.findOneById(idOfObjectToUpvote);
			if(toUpvote == null){
				throw new NotFoundException("collection not found");
			}
			idOfTracker = toUpvote.getUpvoteTrackerId(); 
			break;
		}

		default:
			throw new BadRequestException("unrecognized type");
		}

		if(idOfTracker != null){
			atomicCli.addPrependedItem(idOfTracker, userid, System.currentTimeMillis()); 
			return idOfObjectToUpvote; 
		} else return null;
	}

	@SuppressWarnings("unused")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/downvote")
	@Timed
	public String downvote(String idOfObjectToDownvote, @QueryParam("type") String type, @QueryParam("userid")String userid) throws Exception{
		String idOfTracker = null;
		switch(type){
		case("LOCATION"):{
			KnownLocation toDownvote = locationRepo.findOneById(idOfObjectToDownvote);
			if(toDownvote == null){
				throw new NotFoundException("location not found");
			}
			idOfTracker = toDownvote.getDownvoteTrackerId(); 
			break;
		}
		case("VIDEO"):
		{
			Video toDownvote = vidRepo.findOneById(idOfObjectToDownvote);
			if(toDownvote == null){
				throw new NotFoundException("video not found");
			}
			idOfTracker = toDownvote.getDownvoteTrackerId(); 
			break;
		}

		case("COLLECTION"):
		{
			MediaCollection toDownvote = collRepo.findOneById(idOfObjectToDownvote);
			if(toDownvote == null){
				throw new NotFoundException("collection not found");
			}
			idOfTracker = toDownvote.getDownvoteTrackerId(); 
			break;
		}

		default:
			throw new BadRequestException("unrecognized type");
		}

		if(idOfTracker != null){
			atomicCli.addPrependedItem(idOfTracker, userid, System.currentTimeMillis()); 
			return idOfObjectToDownvote; 
		} else return null;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/getval/{objtype}")
	@Timed
	public String getInfo(@PathParam("objtype")String objType, @QueryParam("type")String typeToGet, @QueryParam("id") String idOfTargetObject) throws Exception{
		//supports UPVOTES, DOWNVOTES, FLAGS, FAVORITES, VIEWS, SKIPS
		String idOfTracker = null;
		switch(objType){
		case("USER"):{
			User toGet = userRepo.findOneById(idOfTargetObject);
			if(toGet == null){
				throw new NotFoundException("user not found");
			}

			switch(typeToGet){
			case("UPVOTES"):
				idOfTracker = toGet.getUpvoteTrackerId();
			break;
			default:
				throw new BadRequestException("invlaid field type");
			}
			break;
		}
		case("LOCATION"):{
			KnownLocation toGet = locationRepo.findOneById(idOfTargetObject);
			if(toGet == null){
				throw new NotFoundException("location not found");
			}
			switch(typeToGet){
			case("UPVOTES"):
				idOfTracker = toGet.getUpvoteTrackerId();
			break;
			case("DOWNVOTES"):
				idOfTracker = toGet.getDownvoteTrackerId();
			break;
			default:
				throw new BadRequestException("invlaid field type");
			}
			break;
		}
		case("VIDEO"):
		{
			Video toGet = vidRepo.findOneById(idOfTargetObject);
			if(toGet == null){
				throw new NotFoundException("video not found");
			}
			switch(typeToGet){
			case("UPVOTES"):
				idOfTracker = toGet.getUpvoteTrackerId();
			break;
			case("DOWNVOTES"):
				idOfTracker = toGet.getDownvoteTrackerId();
			break;
			case("FLAGS"):
				idOfTracker = toGet.getFlagTrackerId();
			break;
			case("FAVORITES"):
				idOfTracker = toGet.getFavoriteCounterId(); 
			break;
			case("SKIPS"):
				idOfTracker = toGet.getSkipCounterId(); 
			break;
			case("VIEWS"):
				idOfTracker = toGet.getViewCounterId(); 
			break;
			default:
				throw new BadRequestException("invlaid field type");
			} 
			break;
		}

		case("COLLECTION"):
		{
			MediaCollection toGet = collRepo.findOneById(idOfTargetObject);
			if(toGet == null){
				throw new NotFoundException("collection not found");
			}
			switch(typeToGet){
			case("UPVOTES"):
				idOfTracker = toGet.getUpvoteTrackerId();
			break;
			case("DOWNVOTES"):
				idOfTracker = toGet.getDownvoteTrackerId();
			break;
			default:
				throw new BadRequestException("invlaid field type");
			}
			break;
		}

		default:
			throw new BadRequestException("unrecognized type");
		}

		if(idOfTracker != null){
			Object result = atomicCli.getContents(idOfTracker); 
			return result.toString(); 
		} else return null;

	}


}