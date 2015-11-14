package com.nostalgia.webserver.auth;


import com.google.common.base.Optional;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

public class SimpleAuthenticator implements Authenticator<BasicCredentials, BasicUser> {
	
	final BasicUser authorized; 

    /**
     * Constructor.
     *
     * @param login user-ID
     * @param password password
     */
    public SimpleAuthenticator(String login, String password) {
        this.authorized = new BasicUser(login, password);
        
    }
	
    @Override
    public Optional<BasicUser> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if (authorized.getPassword().equals(credentials.getPassword())) {
            return Optional.of(new BasicUser(credentials.getUsername()));
        }
        return Optional.absent();
    }
}
