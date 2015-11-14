package com.nostalgia;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cocoahero.android.geojson.GeoJSONObject;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketManager;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.SpatialView;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;
import com.couchbase.client.java.view.*;
import flexjson.JSONDeserializer;

public class LocationRepository {

	// the DB we are using
		private final Cluster cluster; 
		private final Bucket bucket; 
		private final CouchbaseConfig config;
		private final BucketManager bucketManager;
		private static final ObjectMapper mapper = new ObjectMapper();
		

		private static final Logger logger = LoggerFactory.getLogger(LocationRepository.class);
		
		public KnownLocation findOneById(String id) throws Exception {
			JsonDocument found = bucket.get(id);
			if(found == null){
				return null;
			} else return docToLocation(found);

		}
		
		// Initialize design document
		DesignDocument locDoc = DesignDocument.create(
			"location_standard",
			Arrays.asList(
				DefaultView.create("by_name",
					"function (doc, meta) { if (doc.type == 'KnownLocation') { emit(doc.name, null); } }"),
				DefaultView.create("by_channel",
						"function (doc, meta) { "
						+ "if (doc.type == 'KnownLocation') { "
						+ "for (i=0; i < doc.channels.length; i++) {"
						+ "emit(doc.channels[i], null); "
						+ "} "
						+ "} "
						+ "}")
			)
		);
		
		// Initialize design document
				DesignDocument spatialDoc = DesignDocument.create(
					"location_spatial",
					Arrays.asList(
						SpatialView.create("known_points",
								"function (doc, meta) { "
										+ "if (doc.type == 'KnownLocation' && doc.location.lon && doc.location.lat) { "
											+ " emit({ \"type\": \"Point\", \"coordinates\": [doc.geo.lon, doc.geo.lat]}, null);"
									    + "}"
								+ "}")
					)
				);
				
		
		public LocationRepository(CouchbaseConfig userCouchConfig) {
			config = userCouchConfig;
			cluster = CouchbaseCluster.create(userCouchConfig.host);
			bucket = cluster.openBucket(userCouchConfig.bucketName, userCouchConfig.bucketPassword);
			bucketManager = bucket.bucketManager();
			DesignDocument existing = bucketManager.getDesignDocument("location_standard");
			if(existing == null){
				// Insert design document into the bucket
				bucketManager.insertDesignDocument(locDoc);
			}
			
			existing = bucketManager.getDesignDocument("location_spatial");
			if(existing == null){
				// Insert design document into the bucket
				bucketManager.insertDesignDocument(spatialDoc);
			}


		}
	public HashMap<String, KnownLocation> findKnownLocationsCoveringPoint(GeoJSONObject newLoc) {
		SpatialViewQuery query = SpatialViewQuery.from("location_spatial", "known_points");
		SpatialViewResult result = bucket.query(query/*.key(name).limit(10)*/);
		if(!result.success()){
			String error = result.error().toString();
			logger.error("error from view query:" + error);
		}
	

		if (result == null || result.allRows().size() < 1){
			return null;
		}
		
		HashMap<String, KnownLocation> s = new HashMap<String, KnownLocation>();
		for (SpatialViewRow row : result) {
		    JsonDocument matching = row.document();
		    s.put(matching.id().substring(0, 8), docToLocation(matching));
		}
		return s;
	}
	
	public List<KnownLocation> findByName(String name) throws Exception {
		ViewQuery query = ViewQuery.from("location_standard", "by_name").inclusiveEnd(true).key(name);//.stale(Stale.FALSE);
		ViewResult result = bucket.query(query/*.key(name).limit(10)*/);
		if(!result.success()){
			String error = result.error().toString();
			logger.error("error from view query:" + error);
		}
	

		if (result == null || result.totalRows() < 1){
			return null;
		}
		
		ArrayList<KnownLocation> locs = new ArrayList<KnownLocation>();
		for (ViewRow row : result) {
		    JsonDocument matching = row.document();
		    
		    locs.add(docToLocation(matching));
		}

		if(locs.size() > 1){
			logger.error("TOO MANY locations MATCHING NAME");
		}
		return locs;

	}
	
	public static KnownLocation docToLocation(JsonDocument document) {
		JsonObject obj = document.content();
		String objString = obj.toString();
		
		KnownLocation newLoc = new JSONDeserializer<KnownLocation>().deserialize( objString , KnownLocation.class );
		//User result = mapper.convertValue(objString, User.class);
		return newLoc; 
	}
	
	public HashSet<KnownLocation> findByChannel(String channel) {
		HashSet<KnownLocation> s = (HashSet<KnownLocation>) Collections.synchronizedSet(new HashSet<KnownLocation>());
		
		ViewQuery query = ViewQuery.from("location_standard", "by_channel").inclusiveEnd(true).key(channel);//.stale(Stale.FALSE);
		ViewResult result = bucket.query(query/*.key(name).limit(10)*/);
		if(!result.success()){
			String error = result.error().toString();
			logger.error("error from view query:" + error);
		}
		if (result == null || result.totalRows() < 1){
			return null;
		}
		
		for (ViewRow row : result) {
		    JsonDocument matching = row.document();
		    
		   s.add(docToLocation(matching));
		}

		return s;
	}

	public synchronized JsonDocument save(KnownLocation loc) {
		
		String json = null;
		
		try {
			json = mapper.writeValueAsString(loc);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JsonObject jsonObj = JsonObject.fromJson(json);

		JsonDocument  doc = JsonDocument.create(loc.get_id(), jsonObj);
		


		JsonDocument inserted = bucket.upsert(doc);
		return inserted;
	}
	public JsonDocument remove(KnownLocation toDelete) {
		// Remove the document and make sure the delete is persisted.
		JsonDocument doc = bucket.remove(toDelete.get_id());
		return doc;
	}


}
