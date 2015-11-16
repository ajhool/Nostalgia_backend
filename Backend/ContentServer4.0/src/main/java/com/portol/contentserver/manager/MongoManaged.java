package com.portol.contentserver.manager;

import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.portol.contentserver.config.MongoConfig;

import io.dropwizard.lifecycle.Managed;

public class MongoManaged implements Managed{
	
	private MongoClient mongo;
    private DB db;
    
    public MongoManaged(MongoConfig config) throws Exception {
    	MongoCredential cred = MongoCredential.createCredential(config.user, config.db, config.password.toCharArray());
    	ServerAddress srvr = new ServerAddress(config.host, config.port);
    	
    	//Trust all certs
    	
    	SSLSocketFactory sslSocketFactory = null;
    	// Imports: javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager
    	try {
    	    // Create a trust manager that does not validate certificate chains
    	    final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws CertificateException {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws CertificateException {
					// TODO Auto-generated method stub
					
				}
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					// TODO Auto-generated method stub
					return null;
				}
    	    } };
    	    
    	    // Install the all-trusting trust manager
    	    final SSLContext sslContext = SSLContext.getInstance( "SSL" );
    	    sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
    	    // Create an ssl socket factory with our all-trusting manager
    	     sslSocketFactory = sslContext.getSocketFactory();
    	    
    	    

    	    
    	} catch ( final Exception e ) {
    	    e.printStackTrace();
    	}
    	
    	///END TRUST ALL CERTS
    	
    	
    	 MongoClientOptions o = new MongoClientOptions.Builder()
         .socketFactory(sslSocketFactory)
         .build();
    	 
    	 
    	
    	
    	this.mongo = new MongoClient(srvr, Arrays.asList(cred), o);
    	this.db = mongo.getDB(config.db);
    }
    

	public void start() throws Exception {
		
		
	}

	public void stop() throws Exception {
		this.mongo.close();
	
	}
	
	public Mongo getMongo(){
		return this.mongo;
	}
	
	public DB getDB(){
		return db;
	}

}
