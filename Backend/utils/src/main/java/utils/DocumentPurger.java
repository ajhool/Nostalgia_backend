package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.geojson.Feature;
import org.geojson.Point;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketManager;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;

import batch.CouchbaseConfig;

public class DocumentPurger {


	public static void main(String[] args) throws Exception{
		Scanner scanner = new Scanner(System.in);
		setupDB();
		System.out.println("Sure you want to purge all docs from bucket?: " + bucket.name() );
		System.out.println("THIS IS IRREVERSIBLE!. Type \"I accept and want to delete\" below" );
		System.out.print("do you accept the irreversibility of this? " );
		String ans = scanner.nextLine();
		if(ans.equals("I accept and want to delete")){
			deleteAllDocs();
		} else {
			System.out.println("nothing deleted");
		}
		
		System.out.println("Goodbye");
	}

	private static void deleteAllDocs() {
		boolean success = bucketManager.flush();
		System.out.println("Succcessful?: " + success);
	}

	// the DB we are using
	private static Cluster cluster; 
	private static  Bucket bucket; 
	private static  CouchbaseConfig config ;
	private static BucketManager bucketManager;

	private static void setupDB() {
		config = new CouchbaseConfig();
		cluster = CouchbaseCluster.create(config.host);
		bucket = cluster.openBucket("sync_gateway", "passw0rd12");
		bucketManager = bucket.bucketManager();

	}

	private static void tagVideoWithLocations(Video toAdd, Scanner scanner) {
		System.out.println("initing db connection...");
		setupDB();
		System.out.println("Done. Querying for locations...");

		ViewQuery query = ViewQuery.from("location_standard", "by_name");//.stale(Stale.FALSE);
		ViewResult result = bucket.query(query/*.key(name).limit(10)*/);
		if(!result.success()){
			String error = result.error().toString();
			System.err.println("error from view query:" + error);
		}


		ArrayList<JsonDocument> locs = new ArrayList<JsonDocument>();
		for (ViewRow row : result) {
			JsonDocument matching = row.document();
			if(matching != null)
				locs.add(JsonDocument.from(matching, row.id()));
		}

		System.out.println("Locations available: ");

		int index = 0;
		for(JsonDocument doc : locs){
			System.out.println(index + ": " + doc.content().getString("name"));
			index++;
		}

		System.out.print("enter numbers of locations to tag, seperated by a space: ");

		String tags = scanner.nextLine();

		String[] ints = tags.split("\\s+");

		for(String intStr : ints){
			JsonDocument toAddLoc = locs.get(Integer.parseInt(intStr));
			if(toAdd.getLocations() == null){
				toAdd.setLocations(new ArrayList<String>());
			}

			if(!toAdd.getLocations().contains(toAddLoc.id())){
				toAdd.getLocations().add(toAddLoc.id());
			}
		}

		System.out.println("Video tagged with locations: " );

		for(String locStr : toAdd.getLocations()){
			System.out.println(locStr);
		}


	}
	
	final static ObjectMapper mapper = new ObjectMapper();
	static{
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	private static void generateExampleJsonForVideo(File videoJsonDir) throws IOException {
		File outputFile = new File(videoJsonDir, "exampleVideo.json");
		videoJsonDir.mkdirs();
		if(outputFile.exists()){
			System.out.println("example already exists @ " + outputFile.getAbsolutePath());
			System.out.println("no changes made");
			return; 
		} else {
			outputFile.createNewFile();
		}

		Video example = new Video();
		example.set_id("example_id");
		example.setLoads(14);
		example.setSkips(12);
		Point examplePoint = new Point(35.9999999, -79.0096901);
		example.setLocation(examplePoint);
		example.setUrl("<filled serverside>");
		example.setOwnerId("<insert owner id here>");
		example.setProperties(new HashMap<String, String>());
		example.getProperties().put("comment", "example comment");
		example.getProperties().put("sharing_who", "EVERYONE");
		example.getProperties().put("sharing_when", "WIFI");
		example.getProperties().put("sharing_where", "EVERYWHERE");
		example.getProperties().put("video_sound", "MUTE");

		FileWriter writer = new FileWriter(outputFile);

		String videoAsString = mapper.writeValueAsString(example);

		writer.write(videoAsString);
		writer.flush();
		writer.close();

		return; 
	}

	private static ArrayList<Video> scanForVideoFilesIn(File videoJsonDir) throws Exception {
		// set date created where needed and make sure ids are unique
		ArrayList<Video> vids = new ArrayList<Video>();
		HashMap<String, Long> ids = new HashMap<String, Long>();
		ids.put("example_id", 0L);

		String[] extensions = new String[]{"json"};
		Iterator<File> iter = FileUtils.iterateFiles(videoJsonDir, extensions, true);

		ObjectMapper mapper = new ObjectMapper();

		boolean changed = false; 
		while(iter.hasNext()){
			File toProcess= iter.next();
			if(toProcess.getName().equals("exampleVideo.json")) {
				continue;
			}
			Video fileContents = null;
			try {
				fileContents = mapper.readValue(toProcess, Video.class);
			} catch (Exception e) {
				System.err.println("error reading in file: " + toProcess.getName());
				e.printStackTrace();
				continue;
			}

			//set date created if necessary 
			if(fileContents.getDateCreated() < 1000000){
				System.out.println("No date found. creating one...");
				changed = true;
				fileContents.setDateCreated(System.currentTimeMillis());

			}

			//check id
			String id = fileContents.get_id();

			if(ids.keySet().contains(id) && !id.contains("example")){
				System.out.println("duplicate id found.");
				throw new Exception("DUPLICATE IDS: " + id);

			}

			ids.put(id, System.currentTimeMillis());
			vids.add(fileContents);
		}
		return vids; 
	}

	//	private static void addAlexUser() {
	//
	//		Point here = null;
	//
	//
	//		here = new Point(35.9999999, -79.0096901);
	//
	//
	//		LoginRegisterThread register = new LoginRegisterThread("alex@alex.com", "alex", "alex", "app", true, here, "95bcf68b-ddb7-4e1e-82b6-c61d1f759010");
	//		register.start();
	//		try {
	//			register.join();
	//		} catch (InterruptedException e) {
	//			e.printStackTrace();
	//		}
	//
	//		String sessToken;
	//		String region;
	//
	//		try {
	//			sessToken = register.getLoginResponse().getSessionTok();
	//			region = register.getLoginResponse().getRegion();
	//
	//		} catch (NullPointerException e) {
	//			//getLoginResponse might be null;
	//			e.printStackTrace();
	//			//Toast.makeText(this.getActivity(), "Can't connect to user database.", Toast.LENGTH_LONG).show();
	//			System.out.println( "Can't connect to user database.");
	//		}
	//
	//	}

	//	private static void addUSALocation() {
	//		KnownLocation maine = new KnownLocation();
	//		maine.setName("USA");
	//		//	        User uploader = app.getUserRepo().getLoggedInUser();
	//		Feature object = null;
	//		try {
	//			object=   new ObjectMapper().readValue("{\n" +
	//					"  \"type\": \"Feature\",\n" +
	//					"  \"properties\": {\n" +
	//					"    \"name\": \"The USA\",\n" +
	//					"    \"area\": 6000000\n" +
	//					"  },\n" +
	//					"  \"geometry\": {\n" +
	//					"    \"type\": \"Polygon\",\n" +
	//					"    \"coordinates\": [\n" +
	//					"      [\n" +
	//					"        [-131.484375, 24.5271348225978],\n" +
	//					"        [-131.484375, 49.83798245308484],\n" +
	//					"        [-65.390625,  49.83798245308484],\n" +
	//					"        [ -65.390625, 24.5271348225978]\n" +
	//					"      ]\n" +
	//					"    ]\n" +
	//					"  }\n" +
	//					"}   ", Feature.class);
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//
	//		//	        if(uploader == null || object == null){
	//		//	            String msg = "error - must have logged in user to upload location";
	//		//	            Log.e(TAG, msg );
	//		//	            Toast.makeText(MainCaptureActivity.this, msg, Toast.LENGTH_LONG).show();
	//		//	            return;
	//		//	        }
	//
	//		maine.setCreatorId("00000000000000000000000000000000000");
	//
	//		maine.set_id("2f1e7d7b-564d-4d48-9b4d-e4f556b23ed0");
	//
	//		maine.setLocation(object);
	//
	//		KnownLocationCreatorThread creator = new KnownLocationCreatorThread(maine);
	//		creator.start();
	//		// result.closeDrawer();
	//		try {
	//			creator.join();
	//		} catch (InterruptedException e) {
	//			e.printStackTrace();
	//		}
	//
	//		KnownLocation added = creator.getAdded();
	//
	//		// Toast.makeText(MainCaptureActivity.this, "location: " + added.getName() + " added successfully", Toast.LENGTH_LONG).show();
	//		System.out.println("location: " + added.getName() + " added successfully");
	//		return;
	//
	//	}

	//	private static void addNCLocation() {
	//		KnownLocation maine = new KnownLocation();
	//		maine.setName("North Caroline");
	//		//	        User uploader = app.getUserRepo().getLoggedInUser();
	//		Feature object = null;
	//		try {
	//			object=   new ObjectMapper().readValue("{\n" +
	//					"  \"type\": \"Feature\",\n" +
	//					"  \"properties\": {\n" +
	//					"    \"name\": \"North Carolina\",\n" +
	//					"    \"area\": 60000\n" +
	//					"  },\n" +
	//					"  \"geometry\": {\n" +
	//					"    \"type\": \"Polygon\",\n" +
	//					"    \"coordinates\": [\n" +
	//					"      [\n" +
	//					"        [-84.24316406249999, 34.45221847282654],\n" +
	//					"        [-84.24316406249999, 36.76529191711624],\n" +
	//					"        [-75.948486328125,  36.76529191711624],\n" +
	//					"        [-75.948486328125, 34.45221847282654]\n" +
	//					"      ]\n" +
	//					"    ]\n" +
	//					"  }\n" +
	//					"}   ", Feature.class);
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//
	//		//	        if(uploader == null || object == null){
	//		//	            String msg = "error - must have logged in user to upload location";
	//		//	            Log.e(TAG, msg );
	//		//	            Toast.makeText(MainCaptureActivity.this, msg, Toast.LENGTH_LONG).show();
	//		//	            return;
	//		//	        }
	//
	//		maine.setCreatorId("00000000000000000000000000000000000");
	//
	//		maine.set_id("f5852e4e-1f65-4d31-b2e0-3ac453e8b1ba");
	//
	//		maine.setLocation(object);
	//
	//		KnownLocationCreatorThread creator = new KnownLocationCreatorThread(maine);
	//		creator.start();
	//		// result.closeDrawer();
	//		try {
	//			creator.join();
	//		} catch (InterruptedException e) {
	//			e.printStackTrace();
	//		}
	//
	//		KnownLocation added = creator.getAdded();
	//
	//		// Toast.makeText(MainCaptureActivity.this, "location: " + added.getName() + " added successfully", Toast.LENGTH_LONG).show();
	//		System.out.println("location: " + added.getName() + " added successfully");
	//
	//	}

	//	private static void addNALocation() {
	//		KnownLocation maine = new KnownLocation();
	//		maine.setName("North America");
	//		//	        User uploader = app.getUserRepo().getLoggedInUser();
	//		Feature object = null;
	//		try {
	//			object=   new ObjectMapper().readValue("{\n" +
	//					"  \"type\": \"Feature\",\n" +
	//					"  \"properties\": {\n" +
	//					"    \"name\": \"North America\",\n" +
	//					"    \"area\": 60000000\n" +
	//					"  },\n" +
	//					"  \"geometry\": {\n" +
	//					"    \"type\": \"Polygon\",\n" +
	//					"    \"coordinates\": [\n" +
	//					"      [\n" +
	//					"        [-170.15625, 16.46769474828897],\n" +
	//					"        [-170.15625, 77.57995914400348],\n" +
	//					"        [ -54.4921875,  77.57995914400348],\n" +
	//					"        [ -54.4921875, 16.46769474828897]\n" +
	//					"      ]\n" +
	//					"    ]\n" +
	//					"  }\n" +
	//					"}   ", Feature.class);
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}

	//	        if(uploader == null || object == null){
	//	            String msg = "error - must have logged in user to upload location";
	//	            Log.e(TAG, msg );
	//	            Toast.makeText(MainCaptureActivity.this, msg, Toast.LENGTH_LONG).show();
	//	            return;
	//	        }

	//		maine.setCreatorId("00000000000000000000000000000000000");
	//
	//		maine.set_id("5bfb7ea5-54a2-483d-a049-44dc76bb06ca");
	//
	//		maine.setLocation(object);
	//
	//		KnownLocationCreatorThread creator = new KnownLocationCreatorThread(maine);
	//		creator.start();
	//		// result.closeDrawer();
	//		try {
	//			creator.join();
	//		} catch (InterruptedException e) {
	//			e.printStackTrace();
	//		}
	//
	//		KnownLocation added = creator.getAdded();
	//
	//		// Toast.makeText(MainCaptureActivity.this, "location: " + added.getName() + " added successfully", Toast.LENGTH_LONG).show();
	//		System.out.println("location: " + added.getName() + " added successfully");
	//
	//	}

}
