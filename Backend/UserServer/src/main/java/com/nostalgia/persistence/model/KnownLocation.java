package com.nostalgia.persistence.model;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.geojson.GeoJsonObject;

/**
 * Created by alex on 11/4/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnownLocation implements Serializable {

	@JsonIgnore
	public String getChannelName(){
		return _id.substring(0, 8);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -5929855999942363756L;

	private String _id = UUID.randomUUID().toString();

	private String name;
	
	private String creatorId; 


	private GeoJsonObject location;

	private Map<Long, String> sponsoredVideos;

	private Map<String, String> properties;

	private String type = this.getClass().getSimpleName();

	//channels that this document itself is in
	private List<String> channels; 

	private Map<String, String> matchingVideos;

	public KnownLocation(){}


	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GeoJsonObject getLocation() {
		return location;
	}

	public void setLocation(GeoJsonObject location) {
		this.location = location;
	}

	public Map<Long, String> getSponsoredVideos() {
		return sponsoredVideos;
	}

	public void setSponsoredVideos(Map<Long, String> sponsoredVideos) {
		this.sponsoredVideos = sponsoredVideos;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}


	public Map<String, String> getMatchingVideos() {
		return matchingVideos;
	}


	public void setMatchingVideos(Map<String, String> matchingVideos) {
		this.matchingVideos = matchingVideos;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public List<String> getChannels() {
		return channels;
	}


	public void setChannels(List<String> channels) {
		this.channels = channels;
	}
	
	@Override
	public int hashCode(){
		return _id.hashCode();
	}


	public String getCreatorId() {
		return creatorId;
	}


	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}




}
