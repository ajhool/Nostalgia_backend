package com.nostalgia;

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


import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;


import org.xml.sax.SAXException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;


public class S3UlDlClient {

	private BasicAWSCredentials credentials;
	private TransferManager tx;
	private  String bucketName;

	private S3Config config;


	public S3UlDlClient(S3Config s3Config) throws Exception {
		super();
		

//		Properties properties = new Properties();
//		properties.load( getClass().getResourceAsStream("awscredentials") );
//
//		String accessKeyId = properties.getProperty( "accessKey" );
//		String secretKey = properties.getProperty( "secretKey" );
		credentials = new BasicAWSCredentials( "AKIAJDDH56F4J3S7ROQA", "vkw9ts47X5Wql+1wxKTfeqj7tUnCsydmCLrw4yxJ");
		
		bucketName = s3Config.bucketName; 

		this.config = s3Config; 
		tx = new TransferManager(credentials);
		createAmazonS3Bucket();
	}

	private void createAmazonS3Bucket() {
		try {
			if (tx.getAmazonS3Client().doesBucketExist(bucketName) == false) {
				tx.getAmazonS3Client().createBucket(bucketName);
			}
		} catch (AmazonClientException ace) {
			System.err.println("error creating bucket\n" + ace);
		}
	}

	public File getDirFromPending(String dirName, File parentDirToSaveIn){

		MultipleFileDownload myDownload = tx.downloadDirectory(bucketName, config.parentPendingFolder+ "/" + dirName , parentDirToSaveIn);

		
		try {
			myDownload.waitForCompletion();
		} catch (AmazonClientException | InterruptedException e) {
			System.err.println("error waiting for upload completion\n" + e);
			return null;

		}
		
		File saved = new File(parentDirToSaveIn, dirName); 
		
		if(!saved.exists()){
			System.err.println("dir not saved in: " + saved.getAbsolutePath());
		}
		return saved; 	
	}

}
