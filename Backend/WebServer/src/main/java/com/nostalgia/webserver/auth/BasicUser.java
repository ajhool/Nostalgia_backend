package com.nostalgia.webserver.auth;

import java.security.Principal;


public class BasicUser implements Principal{

	private final String name;
	private final String password;
	
	public BasicUser(String login, String password) {
		this.name = login;
		this.password = password; 
	}
	public BasicUser(String username) {
		this.name = username;
		this.password = null;
	}
	@Override
	public String getName() {
		return name;
	}
	public String getPassword() {
		return password;
	}


}
