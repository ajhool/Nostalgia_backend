package com.nostalgia.persistence.model;

import com.cocoahero.android.geojson.GeoJSONObject;

import java.io.Serializable;

/**
 * Created by alex on 11/8/15.
 */
public class LocationUpdate implements Serializable {
    private GeoJSONObject location;
    private String userId;

    public LocationUpdate(GeoJSONObject location, String userId) {
        this.location = location;
        this.userId = userId;
    }

    public LocationUpdate(){}


    public GeoJSONObject getLocation() {
        return location;
    }

    public void setLocation(GeoJSONObject location) {
        this.location = location;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
