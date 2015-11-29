package com.nostalgia.persistence.model;

import java.util.*;

import org.geojson.GeoJsonObject;
import org.geojson.Point;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;

/**
 * Created by alex on 11/4/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {
	@JsonIgnore
	public String getChannelName(){
		return _id.substring(0, 8);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 3672636185090518520L;

	private String type = this.getClass().getSimpleName();

	private String _id = UUID.randomUUID().toString();

	private String name;

	private String password;
	private String homeRegion = "us_east";

	//list of channels user has access to
	private List<String> admin_channels;
	private List<String> admin_roles;

	private Map<String, String> streamTokens; 

	//channel -> time 
	private Map<String, String> video_channels; 

	public Map<String, String> getVideo_channels() {
		return video_channels;
	}


	public void setVideo_channels(Map<String, String> video_channels) {
		this.video_channels = video_channels;
	}
	//channels that this document itself is in
	private List<String> channels; 

	private boolean disabled = false;

	private String email;

	private Point focusedLocation;


	private Point lastKnownLoc;
	private long lastLocationUpdate;

	private long dateJoined;
	private long lastSeen;

	private Map<String, List<String>> publicVideos;

	private Map<String, List<String>> privateVideos;

	private Map<String, List<String>> friendVideos;

	private HashSet<String> location_channels; 

	public HashSet<String> getLocation_channels() {
		return location_channels;
	}


	public void setLocation_channels(HashSet<String> location_channels) {
		this.location_channels = location_channels;
	}


	public HashSet<String> getUser_channels() {
		return user_channels;
	}


	public void setUser_channels(HashSet<String> user_channels) {
		this.user_channels = user_channels;
	}

	private HashSet<String> user_channels; 

	private HashMap<String, String> friends;

	private Map<String, String> settings;

	private Map<String, String> accounts;
	private Map<Long, String> userLocations;
	private String icon;

	private List<String> authorizedDevices;
	private String token;

	private String syncToken;

	@JsonIgnore
	public HashSet<String> purgeOlderThan(long unixTimeStamp){
		if( video_channels == null) return null;
		HashSet<String> removed = new HashSet<String>();
		for(String id : video_channels.keySet()){
			if(Long.parseLong(video_channels.get(id)) < unixTimeStamp){
				//purge
				this.video_channels.remove(id);
				admin_channels.remove(id);
				removed.add(id);
			}
		}
		return removed;
	}

	@JsonIgnore
	public Map<String, String> updateVideoChannels(Set<String> videosToSubscribeTo){
		//clear old locations out from subscriptions
		//all the locations we subscribe to

		if(this.video_channels == null){
			this.video_channels = new HashMap<String, String>();
		}

		if(admin_channels == null){
			admin_channels = new ArrayList<String>();
		}


		for(String exists: this.video_channels.keySet()){

			if(videosToSubscribeTo.contains(exists)){
				//then we were already here. remove it from the list
				videosToSubscribeTo.remove(exists);
			}
			//			} else {
			//

			//			}

		}


		for(String vid : videosToSubscribeTo){
			this.video_channels.put(vid, System.currentTimeMillis() +"");
			admin_channels.add(vid);
		}

		return this.video_channels;

	}

	@JsonIgnore
	public HashSet<String> updateLocationChannels(HashMap<String, KnownLocation> nearbys){
		//clear old locations out from subscriptions
		//all the locations we subscribe to

		if(this.location_channels == null){
			this.location_channels = new HashSet<String>();
		}

		if(admin_channels == null){
			admin_channels = new ArrayList<String>();
		}


		for(String exists: this.location_channels){

			if(nearbys.values().contains(exists)){
				//then we were already here. 
				continue;
			} else {

				this.location_channels.remove(nearbys.get(exists).get_id());
				admin_channels.remove(exists);
			}

		}

		//now, all nearbys has left are new points that arent in existing
		//and exisitng has only the points it has in common with nearbys

		//finally, add in all the nearby points we arent subscribed to yet

		for(KnownLocation loc: nearbys.values()){
			if(!this.location_channels.contains(loc.get_id())){
				this.location_channels.add(loc.get_id());
				admin_channels.add(loc.getChannelName());
			}
		}

		return this.location_channels;

	}

	@JsonIgnore
	public HashSet<String> subscribeToUserChannel(String channelName){
		//clear old locations out from subscriptions
		//all the locations we subscribe to

		if(this.user_channels == null){
			this.user_channels = new HashSet<String>();
		}

		if(admin_channels == null){
			admin_channels = new ArrayList<String>();
		}

		if(this.user_channels.contains(channelName)){
			return this.user_channels;
		} else {
			this.user_channels.add(channelName);
			admin_channels.add(channelName);
		}


		return this.user_channels;

	}

	@JsonIgnore
	public HashSet<String> unsubscribeFromUserChannel(String channelName){
		//clear old locations out from subscriptions
		//all the locations we subscribe to
		HashSet<String> existing = this.user_channels;
		if(existing == null){
			existing = new HashSet<String>();
		}

		if(admin_channels == null){
			admin_channels = new ArrayList<String>();
		}

		if(existing.contains(channelName)){
			existing.remove(channelName);
			admin_channels.remove(channelName);
		} 


		return existing;

	}


	public String getSyncToken(){
		return syncToken;
	}
	public Map<String, String> getAccounts() {
		return accounts;
	}

	public void setAccounts(Map<String, String> accounts) {
		this.accounts = accounts;
	}

	public List<String> getAuthorizedDevices() {
		return authorizedDevices;
	}

	public void setAuthorizedDevices(ArrayList<String> arrayList) {
		this.authorizedDevices = arrayList;
	}


	public User(){
		if(this.userLocations == null){
			userLocations = new HashMap<Long, String>();
		}

	}

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

	public long getDateJoined() {
		return dateJoined;
	}

	public void setDateJoined(long dateJoined) {
		this.dateJoined = dateJoined;
	}

	public Map<String, String> getFriends() {
		return friends;
	}

	public void setFriends(HashMap<String, String> friends) {
		this.friends = friends;
	}

	public Map<String, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, String> settings) {
		this.settings = settings;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public long getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(long lastSeen) {
		this.lastSeen = lastSeen;
	}

	public Point getLastKnownLoc() {
		return lastKnownLoc;
	}

	public void setLastKnownLoc(Point lastKnownLoc) {
		this.lastKnownLoc = lastKnownLoc;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setSyncToken(String session) {
		this.syncToken = session;

	}

	public List<String> getAdmin_roles() {
		return admin_roles;
	}

	public void setAdmin_roles(List<String> admin_roles) {
		this.admin_roles = admin_roles;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public List<String> getAdmin_channels() {
		return admin_channels;
	}

	public void setAdmin_channels(List<String> admin_channels) {
		this.admin_channels = admin_channels;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getChannels() {
		return channels;
	}

	public void setChannels(List<String> channels) {
		this.channels = channels;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHomeRegion() {
		return homeRegion;
	}

	public void setHomeRegion(String homeRegion) {
		this.homeRegion = homeRegion;
	}


	public long getLastLocationUpdate() {
		return lastLocationUpdate;
	}


	public void setLastLocationUpdate(long lastLocationUpdate) {
		this.lastLocationUpdate = lastLocationUpdate;
	}


	public Point getFocusedLocation() {
		return focusedLocation;
	}


	public void setFocusedLocation(Point focusedLocation) {
		this.focusedLocation = focusedLocation;
	}


	public Map<String, String> getStreamTokens() {
		return streamTokens;
	}


	public void setStreamTokens(Map<String, String> streamTokens) {
		this.streamTokens = streamTokens;
	}


	public Map<Long, String> getUserLocations() {
		return userLocations;
	}


	public void setUserLocations(Map<Long, String> userLocations) {
		this.userLocations = userLocations;
	}


	@JsonIgnore
	public Collection<String> subscribeToLocation(String loc_id) {
		//check for duplicate
		if(this.userLocations == null){
			userLocations = new HashMap<Long, String>();
		}

		Collection<String> existing = userLocations.values();
		if(existing.contains(loc_id)){
			//no changes needed
			return existing; 
		}

		//add in location + time it was added
		userLocations.put(System.currentTimeMillis(), loc_id);

		int end = loc_id.indexOf('-');
		String channelName = loc_id.substring(0, end);
		//add in channel ID to allow for subscriptions
		admin_channels.add(channelName);

		return userLocations.values();



	}


	public Map<String, List<String>> getPrivateVideos() {
		return privateVideos;
	}


	public void setPrivateVideos(Map<String, List<String>> privateVideos) {
		this.privateVideos = privateVideos;
	}


	public Map<String, List<String>> getFriendVideos() {
		return friendVideos;
	}


	public void setFriendVideos(Map<String, List<String>> friendVideos) {
		this.friendVideos = friendVideos;
	}


	public Map<String, List<String>> getPublicVideos() {
		return publicVideos;
	}


	public void setPublicVideos(Map<String, List<String>> publicVideos) {
		this.publicVideos = publicVideos;
	}




}
