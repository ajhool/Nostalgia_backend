package com.nostalgia;


public class ProcessorConfig {
	

	private UserCouchConfig userCouchConfig = new  UserCouchConfig();
	private SyncConfig syncConfig = new SyncConfig();
	private LocationCouchConfig locationCouchConfig = new  LocationCouchConfig();
	private VideoCouchConfig videoServConfig = new  VideoCouchConfig();
	
	
	public UserCouchConfig getUserCouchConfig() {
		return userCouchConfig;
	}

	public SyncConfig getSyncConfig() {
		return syncConfig;
	}

	public LocationCouchConfig getLocationCouchConfig() {
		return locationCouchConfig;
	}
	
	public VideoCouchConfig getVideoCouchConfig() {
		return videoServConfig;
	}
	
	
}