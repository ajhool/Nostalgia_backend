package com.portol.contentserver.repository;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.Charsets;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;
import com.mongodb.BasicDBObject;
import com.portol.common.model.Category;
import com.portol.common.model.content.Content;
import com.portol.contentserver.manager.MongoManaged;

public class CategoryRepository {

	//the DB we are using
		private JacksonDBCollection<Category, String> catMongo;

		private static Logger logger = LoggerFactory.getLogger(CategoryRepository.class);
		
	public CategoryRepository(MongoManaged catMongoManaged){
		this.catMongo = JacksonDBCollection.wrap(catMongoManaged.getDB().getCollection("category"), Category.class, String.class);
	}

	
	public Category saveCategory(Category toSave){
		//check for duplicate name
		BasicDBObject query = new BasicDBObject("name", toSave.getName());
		Category existing = catMongo.findOne(query);
		
		if(existing != null){
			logger.warn("warning, overwriting existing category: " + existing.getName());
			catMongo.removeById(existing.getCategoryId());
		}
		
		
		WriteResult<Category, String> result = catMongo.save(toSave);

		if(result != null){
			logger.info("successfully added new category: " + result.getSavedObject().getName());
		}
		return result.getSavedObject();
	}
	
	public List<Category> getAllValidCategories() throws IOException{
		
		
		DBCursor<Category> allCats = catMongo.find();
		
		return allCats.toArray();
//		//TODO provide a real repo that we can update dynamically
//		ArrayList<Category> cats = new ArrayList<Category>();
//
//		Category featured = new Category();
//		featured.setCategoryId("a5642ceb-4ed8-4919-833b-a668968962a0");
//		featured.setName("Featured");
//		featured.setDesc("10000 subscribers");
//
//		URL icon = getClass().getResource("/local/featured.png");
//		byte[] featuredByte = Resources.toByteArray(icon);
//
//		String strEncoded = Base64.getEncoder().encodeToString( featuredByte );
//
//		featured.setIconEncoded(strEncoded);
//
//
//		featured.setPosition(0);
//		featured.setType(Category.VIDEO);
//
//		Category onDemand = new Category();
//		onDemand.setCategoryId("1526f1c7-9655-490b-9a2a-cc26c374cdef");
//		onDemand.setName("All On Demand");
//		onDemand.setDesc("20000 subscribers");
//
//		icon = getClass().getResource("/local/ondemand.png");
//		byte[] bitmapdata1 = Resources.toByteArray(icon);
//
//
//		String strEncoded1 = Base64.getEncoder().encodeToString( bitmapdata1 );
//
//		onDemand.setIconEncoded(strEncoded1);
//
//
//		onDemand.setPosition(1);
//		onDemand.setType(Category.VIDEO);
//
//		Category newVids = new Category();
//		newVids.setCategoryId("f92dfdc0-b9b0-4c1d-a1eb-9f34d2b183d3");
//		newVids.setName("New");
//		newVids.setDesc("30000 subscribers");
//
//		icon = getClass().getResource("/local/new.png");
//		byte[] bitmapdata2 = Resources.toByteArray(icon);
//
//		String strEncoded2 = Base64.getEncoder().encodeToString( bitmapdata2 );
//
//		newVids.setIconEncoded(strEncoded2);
//		newVids.setPosition(2);
//		newVids.setType(Category.VIDEO);
//
//		Category trending = new Category();
//		trending.setCategoryId("37a03ac6-71d9-49ee-b9cb-6df3fdac1435");
//		trending.setName("Trending");
//		trending.setDesc("40000 subscribers");
//
//		icon = getClass().getResource("/local/trending.png");
//		byte[] bitmapdata3 = Resources.toByteArray(icon);
//
//		String strEncoded3 = Base64.getEncoder().encodeToString( bitmapdata3 );
//
//		trending.setIconEncoded(strEncoded3);
//
//		trending.setPosition(3);
//		trending.setType(Category.VIDEO);
//
//		Category popular = new Category();
//		popular.setCategoryId("7d276a26-8b2c-4cf2-bf16-c4c3a80892a5");
//		popular.setName("Popular");
//		popular.setDesc("50000 subscribers");
//
//		icon = getClass().getResource("/local/popular.png");
//		byte[] bitmapdata4 = Resources.toByteArray(icon);
//
//		 String strEncoded4 = Base64.getEncoder().encodeToString( bitmapdata4 );
//			
//	        popular.setIconEncoded(strEncoded4);
//
//		popular.setPosition(4);
//		popular.setType(Category.VIDEO);
//
//		Category mostViewed = new Category();
//		mostViewed.setCategoryId("97cc3b5a-3b9f-416c-9d28-14020f5d8d3d");
//		mostViewed.setName("Most Viewed");
//		mostViewed.setDesc("60000 subscribers");
//
//
//		icon = getClass().getResource("/local/mostviewed.png");
//		byte[] bitmapdata5 = Resources.toByteArray(icon);
//
//		String strEncoded5 = Base64.getEncoder().encodeToString( bitmapdata5 );
//
//		mostViewed.setIconEncoded(strEncoded5);
//
//		mostViewed.setPosition(5);
//		mostViewed.setType(Category.VIDEO);
//
//		Category ourPicks = new Category();
//		ourPicks.setCategoryId("2f1e6beb-6fe1-4003-863c-099bbb18ccce");
//		ourPicks.setName("Our Picks");
//		ourPicks.setDesc("70000 subscribers");
//
//		icon = getClass().getResource("/local/ourpicks.png");
//		byte[] bitmapdata6 = Resources.toByteArray(icon);
//
//		String strEncoded6 = Base64.getEncoder().encodeToString( bitmapdata6 );
//
//		ourPicks.setIconEncoded(strEncoded6);
//
//		ourPicks.setPosition(6);
//		ourPicks.setType(Category.VIDEO);
//
//		cats.add(mostViewed);
//		cats.add(trending);
//		cats.add(newVids);
//		cats.add(onDemand);
//		cats.add(featured);
//		cats.add(ourPicks);
//		cats.add(popular);
//
//		return cats;
	}



}
