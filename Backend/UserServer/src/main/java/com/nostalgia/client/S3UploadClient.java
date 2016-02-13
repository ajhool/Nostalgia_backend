package com.nostalgia.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilderException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.jets3t.service.security.AWSCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.nostalgia.S3Config;
import com.nostalgia.contentserver.model.dash.jaxb.AdaptationSetType;
import com.nostalgia.contentserver.model.dash.jaxb.MPDtype;
import com.nostalgia.contentserver.model.dash.jaxb.RepresentationType;
import com.nostalgia.persistence.model.Video;

import io.dropwizard.lifecycle.Managed;


public class S3UploadClient implements Managed {

	final static Logger logger = LoggerFactory.getLogger(S3UploadClient.class);

	private BasicAWSCredentials credentials;
	private TransferManager tx;
	private  String bucketName;

	private S3Config config;


	public S3UploadClient(S3Config s3Config) throws Exception {
		super();
		

//		Properties properties = new Properties();
//		properties.load( getClass().getResourceAsStream("awscredentials") );
//
//		String accessKeyId = properties.getProperty( "accessKey" );
//		String secretKey = properties.getProperty( "secretKey" );
		credentials = new BasicAWSCredentials( "AKIAJDDH56F4J3S7ROQA", "vkw9ts47X5Wql+1wxKTfeqj7tUnCsydmCLrw4yxJ");
		
		bucketName = s3Config.bucketName; 

		this.config = s3Config; 
		
		
	}

	private void createAmazonS3Bucket() {
		try {
			if (tx.getAmazonS3Client().doesBucketExist(bucketName) == false) {
				tx.getAmazonS3Client().createBucket(bucketName);
			}
		} catch (AmazonClientException ace) {
			logger.error("error creating bucket", ace);
		}
	}

	@Override
	public void start() throws Exception {
		tx = new TransferManager(credentials);
		createAmazonS3Bucket();
	}

	@Override
	public void stop() throws Exception {
		tx.shutdownNow();
	}

	public synchronized boolean uploadDirToS3(File dirToUpload){

		MultipleFileUpload myUpload = tx.uploadDirectory(bucketName, config.parentUploadFolder + "/" + dirToUpload.getName() + "/" , dirToUpload, true);

		try {
			myUpload.waitForCompletion();
		} catch (AmazonClientException | InterruptedException e) {
			logger.error("error waiting for upload completion", e);
			return false;

		}
		return true;	
	}

}
