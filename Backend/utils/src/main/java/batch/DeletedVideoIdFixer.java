package batch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketManager;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.nostalgia.persistence.model.Video;

public class DeletedVideoIdFixer extends BatchClass implements UserBatchClass, LocationBatchClass{



	@Override
	public String getName() {
		return "User video id fixer"; 
	}

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

	@Override
	public Set<JsonDocument> execute(Collection<JsonDocument> input) {
		HashSet<JsonDocument> toSave = new HashSet<JsonDocument>();

		for(JsonDocument original : input){


			String type = original.content().getString("type");

			switch(type){
			case "User":
				scrubExpiredVidsFromUser(original.content());
				break;
			case "KnownLocation":
				scrubExpiredVidsFromLocation(original.content());
				break;
			default:
				System.err.println("error inferring type of document: " + original.id());
				System.exit(1);
			}





			//add to set so that updates get saved
			toSave.add(original);
		}
		return toSave;
	}

	private void scrubExpiredVidsFromLocation(JsonObject content) {
		//for all fields with video ids, check that video actually exists 
		//if not, remove

	}

	private void scrubExpiredVidsFromUser(JsonObject content) {
		//for all fields with video ids, check that video actually exists 
		//if not, remove

	}

}
