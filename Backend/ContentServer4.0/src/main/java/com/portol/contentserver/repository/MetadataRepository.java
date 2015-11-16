package com.portol.contentserver.repository;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.portol.common.model.content.ContentMetadata;
import com.portol.contentserver.manager.MongoManaged;

public class MetadataRepository {
	
	//the DB we are using
	private JacksonDBCollection<ContentMetadata, String> splashMongo;
	public static final Logger logger = LoggerFactory.getLogger(MetadataRepository.class);
	
	public MetadataRepository(MongoManaged splashMongoManaged){
		this.splashMongo = JacksonDBCollection.wrap(splashMongoManaged.getDB().getCollection("splash"), ContentMetadata.class, String.class);
	}

	public ContentMetadata getSplashScreenById(String splashDataId) throws Exception {
		//BasicDBObject query = new BasicDBObject();
		//query.put("_id", splashDataId);
		//DBCursor<ContentMetadata> splashs = splashMongo.find(query);
		ContentMetadata splash = splashMongo.findOneById(splashDataId);
		
//		if(splashs.count() != 1){
//			//TODO
//			throw new Exception("wrong number of splash results found...");
//		}
		
		//return splashs.toArray().get(0);
		return splash;
	}
	
	public ContentMetadata save(ContentMetadata toSave){
		
		BasicDBObject query = new BasicDBObject();
		query.put("parentContentKey", toSave.getParentContentKey());
		DBCursor<ContentMetadata> splashs = splashMongo.find(query);
		
		for(ContentMetadata exist : splashs){
			logger.warn("removing existing splash data for: " + exist.getParentContentKey());
			splashMongo.removeById(exist.getMetadataId());
		}
		
		
		WriteResult<ContentMetadata, String> saved = splashMongo.save(toSave);
		
		logger.info("saved metadata for: " + saved.getSavedObject().getParentContentKey());
		
		return saved.getSavedObject();
		
		
		
		
	}
	
	
	public ContentMetadata getMetadataForParentKey(String parentContentKey) throws Exception {
		BasicDBObject query = new BasicDBObject();
		query.put("parentContentKey", parentContentKey);
		DBCursor<ContentMetadata> splashs = splashMongo.find(query);
		
		if(splashs.count() != 1){
			//TODO
			throw new Exception("wrong number of splash results found...");
		}
		
		return splashs.toArray().get(0);
	}
	
	public ContentMetadata getMetadataForParent(String parentContentId) throws Exception {
		BasicDBObject query = new BasicDBObject();
		query.put("parentContentId", parentContentId);
		DBCursor<ContentMetadata> splashs = splashMongo.find(query);
		
		if(splashs.count() != 1){
			//TODO
			throw new Exception("wrong number of splash results found...");
		}
		
		return splashs.toArray().get(0);
	}

	public void remove(ContentMetadata deleted) {
		
		this.splashMongo.removeById(deleted.getMetadataId());
		
	}
	
	
	

}
