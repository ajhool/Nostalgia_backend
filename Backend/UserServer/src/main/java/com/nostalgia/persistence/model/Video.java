package com.nostalgia.persistence.model;

import com.cocoahero.android.geojson.GeoJSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * Created by alex on 11/4/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Video implements Serializable {
	
	@JsonIgnore
	public String getChannelName(){
		return _id.substring(0, 8);
	}
	
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1435169949509311014L;
	
	private String _id = UUID.randomUUID().toString();
    private String type = this.getClass().getSimpleName();
  
    private long dateCreated;
    boolean enabled = false;

    public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	private int loads;

    private int skips;

    private GeoJSONObject location;

    private Map<String, String> properties;

    private String ownerId;

    private HashSet<String> taggedUserIds;

    private String thumbNail;

    public Video(){
        if(properties == null){
            properties = new HashMap<String, String>();
        }

    }
    public String get_id() {
        return _id;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public GeoJSONObject getLocation() {
        return location;
    }

    public void setLocation(GeoJSONObject location) {
        this.location = location;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getThumbNail() {
        return thumbNail;
    }

    public void setThumbNail(String icon) {
        this.thumbNail = icon;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public HashSet<String> getTaggedUserIds() {
        return taggedUserIds;
    }

    public void setTaggedUserIds(HashSet<String> taggedUserIds) {
        this.taggedUserIds = taggedUserIds;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public int getLoads() {
        return loads;
    }

    public void setLoads(int loads) {
        this.loads = loads;
    }

    public int getSkips() {
        return skips;
    }

    public void setSkips(int skips) {
        this.skips = skips;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
