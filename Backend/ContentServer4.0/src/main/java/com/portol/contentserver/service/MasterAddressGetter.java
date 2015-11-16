package com.portol.contentserver.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.portol.common.model.payment.Payment;
import com.portol.common.model.payment.PaymentInfoRequest;

public class MasterAddressGetter {
	
	static final Logger logger = LoggerFactory.getLogger(MasterAddressGetter.class);
	final Client jClient;
	
	public int port;
	
	public String url;
	
	public String path;
	
	public String uniquePath; 
	
	public String apiKey;
	
	public String password;
	
	public String user;
	
	public MasterAddressGetter(Client client, int port, String url, String path, String apiKey, String password, String user, String uniquePath){
		this.jClient = client;
		
		this.port = port;
		
		this.url = url;
		
		this.path = path;
		
		this.apiKey = apiKey;
		
		this.password = password;
		
		this.user = user;
		
		this.uniquePath = uniquePath;
	}
	
	
	//now, implement the methods we need to interact with the payment server
	
	public Payment getMaster() throws MalformedURLException, IllegalArgumentException, UriBuilderException, URISyntaxException{
		
		URI uri = new URI(url + ":" + port + path.toString());
		logger.info("Querying master payment server for next master @ " + uri.toString());
		
		//assign a mster for this address
       PaymentInfoRequest req = new PaymentInfoRequest();

      
		//these will be checked server side for compliance, so set them right
		req.setApiKey(apiKey);
		
		Payment resp = jClient.target(uri).request().post(Entity.json(req), Payment.class);
		

		logger.info("received reply from paymentserver:\n"+resp.toString());
		
		return resp;
	}
	
	//needs server side support
	public Payment getUniqueMaster() throws MalformedURLException, IllegalArgumentException, UriBuilderException{
		UriBuilder uribuild = UriBuilder.fromUri(url + ":" + port + uniquePath.toString());
		logger.info("Querying master payment server for NEW master @ " + uribuild.build().toURL());
		
		//assign a mster for this address
       PaymentInfoRequest req = new PaymentInfoRequest();

      
		//these will be checked server side for compliance, so set them right
		req.setApiKey(apiKey);
		
		Payment resp = jClient.target(uribuild).request().post(Entity.json(req), Payment.class);
		logger.info("request completed");

		logger.info("received reply for new master from paymentserver:\n"+resp.toString());
		
		return resp;
	}
}
