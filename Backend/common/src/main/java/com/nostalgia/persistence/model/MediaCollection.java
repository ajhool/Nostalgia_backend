package com.nostalgia.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.geojson.Feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by alex on 11/4/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaCollection implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3372689185090518529L;


    private long versionNumber;
    @JsonIgnore
    public String getChannelName(){
        return _id.substring(0, 8);
    }

    private String _id = UUID.randomUUID().toString();

    private String type = MediaCollection.class.getName();

    private String name;

    //channels that this document itself is in
    private List<String> channels;
    private Map<String, String> matchingVideos;
    private String creatorId;

    private Map<String, String> properties;

    private List<String> thumbnails;
    private List<String> tags;
    private List<String> locations;

    boolean publicColl;
    private String linkedLocation;
    private Set<String> readers;
    private Set<String> writers;



    public MediaCollection(){
        if(channels == null){
            channels = new ArrayList<String>();
            channels.add(this.getChannelName());
        }

        if(thumbnails == null){
            thumbnails = new ArrayList<String>();
        }

        if(matchingVideos == null){
            matchingVideos = new HashMap<String, String>();
        }

        if(tags == null){
            tags = new ArrayList<String>();
        }

        if(locations == null){
            locations = new ArrayList<String>();
        }
        if(readers == null){
            readers = new HashSet<String>();
        }
        if(writers == null){
            writers = new HashSet<String>();
        }

    }


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
        this.channels.clear();
        channels.add(this.getChannelName());

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isPublicColl() {
        return publicColl;
    }

    public void setPublicColl(boolean publicColl) {
        this.publicColl = publicColl;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }


    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }


    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public Map<String, String> getMatchingVideos() {
        return matchingVideos;
    }

    public void setMatchingVideos(Map<String, String> matchingVideos) {
        this.matchingVideos = matchingVideos;
    }

    @Override
    @JsonIgnore
    public String toString(){
        StringBuffer buf = new StringBuffer();
        buf.append("Name: " + name + "\n");
        buf.append("ID: " + _id + "\n");

        buf.append("Videos: \n");
        if(this.matchingVideos != null && !matchingVideos.isEmpty()) {
            for (String key : matchingVideos.keySet()){
                buf.append("Video #" + key + ": " + matchingVideos.get(key) + "\n");
            }

        } else {
            buf.append("no videos");
        }

        return buf.toString();
    }

    public List<String> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(List<String> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public long getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(long versionNumber) {
        this.versionNumber = versionNumber;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Set<String> getReaders() {
        return readers;
    }

    public void setReaders(Set<String> readers) {
        this.readers = readers;
    }

    public Set<String> getWriters() {
        return writers;
    }

    public void setWriters(Set<String> writers) {
        this.writers = writers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


	public String getLinkedLocation() {
		return linkedLocation;
	}


	public void setLinkedLocation(String linkedLocation) {
		this.linkedLocation = linkedLocation;
	}
}
