package com.nostalgia.client;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.document.LegacyDocument;
import com.nostalgia.CouchbaseConfig;

public class AtomicOpsClient {

	//todo make atomics bucket
	private final Bucket bucket;
	private CouchbaseConfig config;
	private CouchbaseCluster cluster; 
	
	public AtomicOpsClient(CouchbaseConfig conf){
		config = conf;
		cluster = CouchbaseCluster.create(conf.host);
		bucket = cluster.openBucket(conf.bucketName, conf.bucketPassword);
	
	}
	
	public long incrementCounter(String counterId){
		 JsonLongDocument rv = bucket.counter(counterId, 1, 0);
		 return rv.content();
	}
	
	public long decrementCounter(String counterId){
	
		 JsonLongDocument rv = bucket.counter(counterId, -1, 0);
		 return rv.content();
	}
	
	public boolean addPrependedItem(String trackerId, String voterId, long voteTime){
		Document existing = bucket.get(trackerId); 
		if(existing == null){
		LegacyDocument initial = LegacyDocument.create(trackerId);
		bucket.insert(initial);
		}
		
		LegacyDocument toPrepend = LegacyDocument.create(trackerId, "{" + voterId + ", " + voteTime +"}");
	
		LegacyDocument prepended = bucket.prepend(toPrepend);

		return prepended.id().equals(toPrepend.id());
	}

	public Object getContents(String idOfTracker) {
		Document doc = bucket.get(idOfTracker);
		if(doc == null){
			return null; 
		}
		return doc.content();
	}
	
	
	
	
}