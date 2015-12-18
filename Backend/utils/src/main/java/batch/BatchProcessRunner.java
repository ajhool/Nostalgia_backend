package batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketManager;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
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
import com.nostalgia.persistence.model.User;

public class BatchProcessRunner {

	final static BatchClass[] batchSources = new BatchClass[]{
			new NonAwsVideoJsonGetter()
	};

	// the DB we are using
	private static Cluster cluster; 
	private static  Bucket bucket; 
	private static  CouchbaseConfig config ;
	private static BucketManager bucketManager;

	private static void setupDB() {
		config = new CouchbaseConfig();
		cluster = CouchbaseCluster.create(config.host);
		bucket = cluster.openBucket(config.bucketName, config.bucketPassword);
		bucketManager = bucket.bucketManager();

	}

	public static void main(String[] args) throws Exception{
		System.out.println("Hello. welcome to the alex batch script runner");
		Thread.sleep(1000);
		System.out.println("The available scripts to execute are: ");

		int index = 0; 
		for(BatchClass clazz : batchSources){
			System.out.println(index + ": " +clazz.getName());
			index++;
		}

		System.out.print("enter number of script to run: ");

		Scanner in = new Scanner(System.in);

		int selection = in.nextInt(); 

		BatchClass toExecute = batchSources[selection];
		setupDB() ;
		Map<String, JsonDocument> allOfType = null;

		if(toExecute instanceof VideoBatchClass){
			allOfType = getAllVideoDocuments();
		} else if(toExecute instanceof LocationBatchClass){
			allOfType = getAllLocationDocuments(); 
		} else if(toExecute instanceof UserBatchClass){
			allOfType = getAllUserDocuments();
		} else throw new Exception("Batch script: " + toExecute.getName() + " must extend valid superclass");

		Collection<JsonDocument> copied = new ArrayList<JsonDocument>();

		for(JsonDocument orig : allOfType.values()){
			copied.add(JsonDocument.from(orig, orig.id()));
		}

		Set<JsonDocument> modded = batchSources[selection].execute(copied);

		//delete all missing 
		for(JsonDocument orig: allOfType.values()){
			if(!modded.contains(orig)){
				//then the document was deleted in the batch script
				deleteDocument(orig);
				allOfType.remove(orig).id();
			}
		}

		//save all changed docs

		for(JsonDocument mod : modded){
			JsonDocument original = allOfType.get(mod.id());
			if(!original.equals(mod)){
				update(mod);

			}

		}

	}


	private static boolean update(JsonDocument mod) {
		System.out.println("updating document: " + mod.id());
		JsonDocument updated = bucket.upsert(mod);
		
		return updated.id().equals(mod.id());


	}


	private static boolean deleteDocument(JsonDocument orig) {
		System.out.println("Deleting document: " + orig.id());
		JsonDocument removed = bucket.remove(orig.id());
		return removed.id().equals(orig.id());
		
	}


	private static Map<String, JsonDocument> getAllUserDocuments() {
	
		ViewQuery query = ViewQuery.from("user", "all_users");//.stale(Stale.FALSE);
		ViewResult result = bucket.query(query/*.key(name).limit(10)*/);
		if(!result.success()){
			String error = result.error().toString();
			System.err.println("error from view query:" + error);
		}
	

		if (result == null || result.totalRows() < 1){
			return null;
		}
		
		HashMap<String, JsonDocument> users = new HashMap<String, JsonDocument>();
		for (ViewRow row : result) {
		    JsonDocument matching = row.document();
		    
		    users.put(row.id(), JsonDocument.from(matching, row.id()));
		}

		return users;
		
		
		
	}


	private static Map<String, JsonDocument> getAllVideoDocuments() {
		ViewQuery query = ViewQuery.from("video_standard", "by_id");//.stale(Stale.FALSE);
		ViewResult result = bucket.query(query/*.key(name).limit(10)*/);
		if(!result.success()){
			String error = result.error().toString();
			System.err.println("error from view query:" + error);
		}
	

		if (result == null || result.totalRows() < 1){
			return null;
		}
		
		HashMap<String, JsonDocument> videos = new HashMap<String, JsonDocument>();
		for (ViewRow row : result) {
		    JsonDocument matching = row.document();
		    
		    videos.put(row.id(), JsonDocument.from(matching, row.id()));
		}
		return videos; 
	}


	private static Map<String, JsonDocument> getAllLocationDocuments() {
		ViewQuery query = ViewQuery.from("location_standard", "by_name");//.stale(Stale.FALSE);
		ViewResult result = bucket.query(query/*.key(name).limit(10)*/);
		if(!result.success()){
			String error = result.error().toString();
			System.err.println("error from view query:" + error);
		}
	

		if (result == null || result.totalRows() < 1){
			return null;
		}
		
		HashMap<String, JsonDocument> locs = new HashMap<String, JsonDocument>();
		for (ViewRow row : result) {
		    JsonDocument matching = row.document();
		    
		    locs.put(row.id(), JsonDocument.from(matching, row.id()));
		}
		return locs; 
	}
}
