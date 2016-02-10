package com.nostalgia.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.JsonProcessingException;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.nostalgia.LambdaAPIConfig;
import com.nostalgia.S3Config;
import com.nostalgia.contentserver.model.dash.jaxb.AdaptationSetType;
import com.nostalgia.contentserver.model.dash.jaxb.MPDtype;
import com.nostalgia.contentserver.model.dash.jaxb.RepresentationType;
import com.nostalgia.persistence.model.ProcessContentRequestPojo;
import com.nostalgia.persistence.model.ProcessContentResponsePojo;
import com.nostalgia.persistence.model.Video;

import io.dropwizard.lifecycle.Managed;


public class LambdaClient {

	final static Logger logger = LoggerFactory.getLogger(LambdaClient.class);

	final private String lambdaAPIKey = "04kxnTSFC12GyeGZAma6J3dLdzHfXEZ57YEpp6dJ";
	private static final ObjectMapper mapper = new ObjectMapper(); 
	private LambdaAPIConfig config;

	private HttpClient client; 

	public LambdaClient(LambdaAPIConfig config, HttpClient client) throws Exception {
		super();
		this.client = client; 
		this.config = config; 
	}

	public List<String> processVideo(String videoIdToProcess) throws Exception {
		String URL = "https://d215uwglt7.execute-api.us-east-1.amazonaws.com/prod/LambdaContentProcessor"; 
		ProcessContentResponsePojo respObj = null; 
		HttpPost httpPost = null;

		httpPost = new HttpPost(URL);

		ProcessContentRequestPojo input = new ProcessContentRequestPojo();
		input.id = videoIdToProcess;
		String toSend = mapper.writeValueAsString(input);
		try {
			
			httpPost.setEntity(new StringEntity(toSend));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpPost.setHeader("x-api-key", this.lambdaAPIKey);


		HttpResponse resp = null;
		try {
			resp = client.execute(httpPost);
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if(resp.getEntity()!= null) {
			String contents = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
			respObj = mapper.readValue(contents, ProcessContentResponsePojo.class);
		}

		return respObj.generated_files; 

	}



}
