package com.portol.contentserver.repository;

import java.util.ArrayList;
import java.util.List;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.DBQuery.Query;




import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.common.model.content.Content;
import com.portol.contentserver.manager.MongoManaged;

public class ContentRepository {

	//the DB we are using
	private JacksonDBCollection<Content, String> contentMongo;

	private static Logger logger = LoggerFactory.getLogger(ContentRepository.class);
	public ContentRepository(MongoManaged contentMongoManaged){
		this.contentMongo = JacksonDBCollection.wrap(contentMongoManaged.getDB().getCollection("content"), Content.class, String.class);
	}

	public Content findByVideoKey(String videoKey) throws Exception {
		DBCursor<Content> cursor = null;
		Query query = DBQuery.is("contentKey", videoKey);
		cursor = contentMongo.find(query);

		if(cursor.size() == 1) return cursor.toArray().get(0);
		if(cursor.size() == 0) return null;
		
		//otherwise there are multiple results
		
		
		do{
			
			
			int version = 0;

			while(cursor.size() > 0){
				version++;
			 query = DBQuery.is("contentKey", videoKey + "v" + version);
			cursor = contentMongo.find(query);
			
			}
			
			logger.warn("duplicate content pieces found. Saving old versions starting at number: " + version);
			List<Content> matches = cursor.toArray();

			Content newest = matches.get(0);

			for(int i = 0; i < matches.size(); i++){
				Content cur = matches.get(i);
				if(cur.getCreated().after(newest.getCreated())){
					//then cur is newer
					newest = cur;

					//check again to be sure
					i=-1;
				} else {
					//remove 
					matches.remove(i);
					i = -1;

					cur.setContentKey(cur.getContentKey() + "v" + version);
					logger.warn("changed content id " + cur.getId() + " to have version number: " + version);
					version++;
					contentMongo.save(cur);

					

				}



			}

			//reset cursor
			query = DBQuery.is("contentKey", videoKey);
			cursor = contentMongo.find(query);

		}while(cursor.count() > 1);

		return cursor.toArray().get(0);
	}

	
	public synchronized Content save(Content toSave){
		WriteResult<Content, String> result = this.contentMongo.save(toSave);
		return result.getSavedObject();
	}
	public Content removeContent(Content toRemove){
		WriteResult<Content, String> result = contentMongo.removeById(toRemove.getId());
		return result.getSavedObject();
	}

	public Content findById(String Id) {

		return contentMongo.findOneById(Id);
	}

}
