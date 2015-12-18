package batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketManager;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.view.DesignDocument;

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


	private static void update(JsonDocument mod) {
		System.out.println("updating document: " + mod.id());



	}


	private static void deleteDocument(JsonDocument orig) {
		System.out.println("Deleting document: " + orig.id());

	}


	private static Map<String, JsonDocument> getAllUserDocuments() {
		// TODO Auto-generated method stub
		return null;
	}


	private static Map<String, JsonDocument> getAllVideoDocuments() {
		// TODO Auto-generated method stub
		return null;
	}


	private static Map<String, JsonDocument> getAllLocationDocuments() {
		// TODO Auto-generated method stub
		return null;
	}
}
