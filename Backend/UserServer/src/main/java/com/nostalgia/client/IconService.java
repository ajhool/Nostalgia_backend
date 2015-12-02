package com.nostalgia.client;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.nostalgia.IconServiceConfig;
import com.nostalgia.persistence.model.icon.IconReply;



public class IconService {

	private final IconServiceConfig conf;
	private final Client icComm; 
	private static final ObjectMapper mapper = new ObjectMapper();
	
	
	public IconService(IconServiceConfig conf, Client icClient){
		this.conf = conf;
		this.icComm = icClient;
	}
	
	private static Logger logger = LoggerFactory.getLogger(IconService.class);
	
	
	
	public String getBase64Icon(String key) throws IOException{

		
		UriBuilder uribuild = UriBuilder.fromUri(conf.iconHost + ":" + conf.port + conf.newIconPath + "?key=bar");
		
		IconReply resp = null;
		
		try{
			resp = icComm.target(uribuild).request().post(Entity.text(key), IconReply.class);
	} catch (Exception e){
		logger.error("error retrieving icon from icon microservice. Using default Icon instead", e);
		 resp = new IconReply();
		resp.setUsingDefault(true);
		
		URL icon = getClass().getResource("/local/defaultUserIcon.png");
		byte[] defaultEncoded = Resources.toByteArray(icon);
		resp.setEncodedImage(defaultEncoded);
		
	}
		return  new String(Base64.encodeBase64(resp.getEncodedImage(), false, false));
	}
	
	
}
