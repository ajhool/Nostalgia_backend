package com.nostalgia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.geojson.GeoJsonObject;
import org.geojson.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketManager;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.SpatialView;
import com.couchbase.client.java.view.SpatialViewQuery;
import com.couchbase.client.java.view.SpatialViewResult;
import com.couchbase.client.java.view.SpatialViewRow;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;

import flexjson.JSONDeserializer;

public class VideoRepository {

	private static final Logger logger = LoggerFactory.getLogger(VideoRepository.class);

	// the DB we are using
	private final Cluster cluster; 
	private final Bucket bucket; 
	private final VideoCouchConfig config;
	private final BucketManager bucketManager;
	private static final ObjectMapper mapper = new ObjectMapper();

//	// Initialize design document
//	DesignDocument vidDoc = DesignDocument.create(
//			"video_attrs_process",
//			Arrays.asList(
////					DefaultView.create("by_id",
////							"function (doc, meta) { if (doc.type == 'Video') { emit(doc._id, null); } }"),
//					//			DefaultView.create("by_channel",
//					//					"function (doc, meta) { "
//					//					+ "if (doc.type == 'Video') { "
//					//					+ "for (i=0; i < doc.channels.length; i++) {"
//					//					+ "emit(doc.channels[i], null); "
//					//					+ "} "
//					//					+ "} "
//					//					+ "}")
//
//
//					DefaultView.create("by_status",
//							"function (doc, meta) { "
//									+ "if (doc.type == 'Video' && doc.status) { "
//									+ "emit(doc.status, null); "
//									+ "} "
//									+ "}")
//					)
//			);
//
//	// Initialize design document
//	DesignDocument spatialDoc = DesignDocument.create(
//			"video_spatial",
//			Arrays.asList(
//					SpatialView.create("video_points",
//							"function (doc, meta) { "
//									+ "if (doc.type == 'Video' && doc.location.coordinates) { "
//									+ " emit(doc.location, null);"
//									+ "}"
//									+ "}")
//					)
//			);

	public VideoRepository(VideoCouchConfig videoCouchConfig) throws Exception {
		config = videoCouchConfig;
		cluster = CouchbaseCluster.create(config.host);
		bucket = cluster.openBucket(config.bucketName, config.bucketPassword);
		bucketManager = bucket.bucketManager();
		DesignDocument existing = bucketManager.getDesignDocument("video_standard");
		if(existing == null){
			throw new Exception("No video_standard view found!");
		}

		existing = bucketManager.getDesignDocument("video_spatial");
		if(existing == null){
			throw new Exception("No video_spatial view found!");
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
	
	public ArrayList<JsonDocument> getAllPendingVideos(){
		ViewQuery query = ViewQuery.from("video_standard", "by_status").inclusiveEnd(true).key("PENDING");//.stale(Stale.FALSE);
		ViewResult result = bucket.query(query/*.key(name).limit(10)*/);
		if(!result.success()){
			String error = result.error().toString();
			logger.error("error from view query:" + error);
		}
	

		if (result == null || result.totalRows() < 1){
			return null;
		}
		
		ArrayList<JsonDocument> vids = new ArrayList<JsonDocument>();
		for (ViewRow row : result) {
		    JsonDocument matching = row.document();
		    
		    vids.add(matching);
		}

		if(vids.size() > 1){
			logger.error("TOO MANY USERS MATCHING NAME");
		}
		return vids;
	}

	public JsonDocument deleteVideo(String videoToDelete){
		try {
          return bucket.remove(videoToDelete);
		} catch (Exception e){
			return null; 
		}
	}
	public HashMap<String, Video> findVideosWithin(Polygon hasbbox) {
		double[] bbox = hasbbox.getBbox(); 

		if(bbox == null){
			bbox = LocationRepository.buildbbox(hasbbox);
		}
		if(bbox == null || bbox.length < 4){
			logger.error("only bounding box based queries supported at this time");
			return null;
		}
		JsonArray START = JsonArray.from(bbox[1], bbox[0]);
		JsonArray END = JsonArray.from(bbox[3], bbox[2]);
		SpatialViewQuery query = SpatialViewQuery.from("video_spatial", "video_points").range(START, END);
		SpatialViewResult result = bucket.query(query/*.key(name).limit(10)*/);
		if(!result.success()){
			String error = result.error().toString();
			logger.error("error from view query:" + error);
			return null;
		}

		List<SpatialViewRow> rows = result.allRows();

		HashMap<String, Video> s = new HashMap<String, Video>();
		for (SpatialViewRow row : rows) {
			JsonDocument matching = row.document();
			s.put(matching.id().substring(0, 8), docToVideo(matching));
		}
		return s;
	}
}
