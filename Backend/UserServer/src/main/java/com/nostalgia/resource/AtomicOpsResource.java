package com.nostalgia.resource;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;

import com.nostalgia.UserRepository;
import com.nostalgia.client.AtomicOpsClient;

public class AtomicOpsResource {

	public AtomicOpsResource(UserRepository userRepo, AtomicOpsClient atomicCli) {
		// TODO Auto-generated constructor stub
	}
	
	public String favoriteVideo(String videoId, @QueryParam("userid")String userid){
		//save to user
		
		//increment counter
	}
	
	public String flagVideo(String videoId, @QueryParam("userid")String userid){
		//add report
	}
	
	public String skippedVideo(String videoId, @QueryParam("userid")String userid){

		//increment counter
	}
	
	public String viewedVideo(String videoId, @QueryParam("userid")String userid){
		//increment counter
	}
	
	public String upvote(String idOfObjectToUpvote, @QueryParam("userid")String userid){
		//add report
	}
	
	public String downvote(String idOfObjectToDownvote, @QueryParam("userid")String userid){
		//add report
	}
	
	@GET
	public String getInfo(@QueryParam("type")String typeToGet, @QueryParam("id") String idOfTargetObject){
		//supports UPVOTES, DOWNVOTES, FLAGS, FAVORITES, VIEWS, SKIPS
	}
	

}
