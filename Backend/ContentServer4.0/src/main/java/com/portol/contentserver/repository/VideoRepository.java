package com.portol.contentserver.repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketManager;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.SpatialView;
import com.couchbase.client.java.view.SpatialViewQuery;
import com.couchbase.client.java.view.SpatialViewResult;
import com.couchbase.client.java.view.SpatialViewRow;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.Video;

import flexjson.JSONDeserializer;

public class VideoRepository {

	private static final Logger logger = LoggerFactory.getLogger(VideoRepository.class);
	
	// the DB we are using
	private final Cluster cluster; 
	private final Bucket bucket; 
	private final CouchbaseConfig config;
	private final BucketManager bucketManager;
	private static final ObjectMapper mapper = new ObjectMapper();
	
	// Initialize design document
	DesignDocument vidDoc = DesignDocument.create(
		"video_standard",
		Arrays.asList(
			DefaultView.create("by_name",
				"function (doc, meta) { if (doc.type == 'Video') { emit(doc.name, null); } }")//,
//			DefaultView.create("by_channel",
//					"function (doc, meta) { "
//					+ "if (doc.type == 'Video') { "
//					+ "for (i=0; i < doc.channels.length; i++) {"
//					+ "emit(doc.channels[i], null); "
//					+ "} "
//					+ "} "
//					+ "}")
		)
	);
	
	// Initialize design document
			DesignDocument spatialDoc = DesignDocument.create(
				"video_spatial",
				Arrays.asList(
					SpatialView.create("video_points",
							"function (doc, meta) { "
									+ "if (doc.type == 'Video' && doc.location.lon && doc.location.lat) { "
										+ " emit({ \"type\": \"Point\", \"coordinates\": [doc.geo.lon, doc.geo.lat]}, null);"
								    + "}"
							+ "}")
				)
			);
			
	public VideoRepository(CouchbaseConfig videoCouchConfig) {
		config = videoCouchConfig;
		cluster = CouchbaseCluster.create(config.host);
		bucket = cluster.openBucket(config.bucketName, config.bucketPassword);
		bucketManager = bucket.bucketManager();
		DesignDocument existing = bucketManager.getDesignDocument("video_standard");
		if(existing == null){
			// Insert design document into the bucket
			bucketManager.insertDesignDocument(vidDoc);
		}
		
		existing = bucketManager.getDesignDocument("video_spatial");
		if(existing == null){
			// Insert design document into the bucket
			bucketManager.insertDesignDocument(spatialDoc);
		}

	}

	public JsonDocument save(Video adding) {

		String json = null;
		try {
			json = mapper.writeValueAsString(adding);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JsonObject jsonObj = JsonObject.fromJson(json);

		JsonDocument  doc = JsonDocument.create(adding.get_id(), jsonObj);

		JsonDocument inserted = bucket.upsert(doc);
		return inserted; 
	}

	public Video findOneById(String id) {
		JsonDocument found = bucket.get(id);
		if(found == null){
			return null;
		} else return docToVideo(found);
	}
	public static Video docToVideo(JsonDocument document) {
		JsonObject obj = document.content();
		String objString = obj.toString();
		
		Video newVid = null;
		try {
			newVid = mapper.readValue( objString , Video.class );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//User result = mapper.convertValue(objString, User.class);
		return newVid; 
	}
	
	public HashMap<String, Video> findVideosCoveringPoint(GeoJsonObject point) {
		SpatialViewQuery query = SpatialViewQuery.from("video_spatial", "video_points");
		SpatialViewResult result = bucket.query(query/*.key(name).limit(10)*/);
		if(!result.success()){
			String error = result.error().toString();
			logger.error("error from view query:" + error);
		}
	

		if (result == null || result.allRows().size() < 1){
			return null;
		}
		
		HashMap<String, Video> s = new HashMap<String, Video>();
		for (SpatialViewRow row : result) {
		    JsonDocument matching = row.document();
		    s.put(matching.id().substring(0, 8), docToVideo(matching));
		}
		return s;
	}
}