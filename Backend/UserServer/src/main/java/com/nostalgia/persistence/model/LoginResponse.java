package com.nostalgia.persistence.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4730448086571677596L;
	private String sessionTok;
	private String region; 

	public LoginResponse(){}
	
	public String getSessionTok() {
		return sessionTok;
	}

	public void setSessionTok(String sessionTok) {
		this.sessionTok = sessionTok;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	} 
	

}
