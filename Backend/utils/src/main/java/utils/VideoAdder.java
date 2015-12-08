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
import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.geojson.Feature;
import org.geojson.Point;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;

public class VideoAdder {



	public static void main(String[] args) throws IOException, InterruptedException{
		Scanner scanner = new Scanner(System.in);
		boolean running = true;
		File operatingDir = new File(System.getProperty("user.dir"));
		File videoJsonDir = new File(operatingDir, "videos");
		File videoDataDir = new File(operatingDir, "videodata");

		if(!videoJsonDir.exists()){
			videoJsonDir.mkdirs();
		}
		System.out.println("Welcome to the video adder. Scanning for .json files in: " + videoJsonDir.getAbsolutePath() + "...");
		while (running){


			ArrayList<Video> scannedVideos = scanForVideoFilesIn(videoJsonDir);
			System.out.println("Videos found: ");

			for(int i = 0; i < scannedVideos.size(); i++){
				Video cur = scannedVideos.get(i);
				String comment = "none";
				if(cur.getProperties() != null){
					for(String key : cur.getProperties().keySet()){
						if(key.contains("comment") || key.contains("thought")){
							comment = cur.getProperties().get(key);
						}
					}
				}
				System.out.println(i + ": Video Id: " + scannedVideos.get(i).get_id());
				System.out.println("      with comment: " + comment);
				System.out.println("      created on: " + cur.getDateCreated());
				System.out.println("      at location: " + cur.getLocation());
			}

			if(scannedVideos.size() == 0){
				System.out.println("None.");
			}
			System.out.println("\n");
			System.out.print("add which video #? (q to quit, e for example gen): ");
			String selection = null;

			selection = scanner.nextLine();
			int asNum = -1;
			try {
				asNum = Integer.parseInt(selection);
			} catch (Exception e){
				switch(selection){

				case("q"):
					System.out.println("Goodbye");
				    System.exit(0);
				break;

				case("e"):
					generateExampleJsonForVideo(videoJsonDir);
				continue;

				default:
					System.out.println("Error - command not recognized: " + selection);
					Thread.sleep(150);
					continue;
				}
			}

			Video toAdd = scannedVideos.get(asNum);

			System.out.println("Adding video: " + toAdd.get_id());

			System.out.println("Enter the path to the video's data, or [ENTER] to search in: " + videoDataDir.getAbsolutePath());
			System.out.println("Note - the data will be copied into the data dir if alternate path specified");

			String path = scanner.nextLine();

			String searchPath = null;

			if(path.length() < 2){
				System.out.println("Searching in: " + videoDataDir.getAbsolutePath() + " for file: " + toAdd.get_id() + ".mp4");
				searchPath = videoDataDir.getAbsolutePath() + "/" + toAdd.get_id() + ".mp4" ;

			} else {
				System.out.println("Searching in: " + path);
				searchPath = path;
			}

			File data = new File(searchPath);
			if(!data.exists()){
				System.err.println("Error - no file found at: " + path);
				continue;
			}

			//we have a data file we know exists 

			System.out.println("File found @" + data.getAbsolutePath());

			File saved = new File(videoDataDir, toAdd.get_id());
			if(!data.getAbsolutePath().contains(videoDataDir.getName())){
				//copy
				System.out.println("Copying file...");
				FileUtils.copyFile(data, saved);
				System.out.println("done");
			}

			VideoUploadTask task = new VideoUploadTask(saved.getAbsolutePath(), toAdd);
			task.start();
			task.join();

			System.out.println("video uploaded successfully");

		}
		scanner.close();
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
		example.setMpd("<filled serverside>");
		example.setOwnerId("<insert owner id here>");
		example.setProperties(new HashMap<String, String>());
		example.getProperties().put("comment", "example comment");

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		FileWriter writer = new FileWriter(outputFile);

		String videoAsString = mapper.writeValueAsString(example);

		writer.write(videoAsString);
		writer.flush();
		writer.close();

		return; 
	}

	private static ArrayList<Video> scanForVideoFilesIn(File videoJsonDir) throws JsonParseException, JsonMappingException, IOException {
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

			if(ids.keySet().contains(id)){
				System.out.println("duplicate id found. re-writing to use unique id");
				changed = true;

				if(ids.get(id) < fileContents.getDateCreated()){
					//then keep the exisiting file's id and re-write this id
					fileContents.set_id(UUID.randomUUID().toString());
					toProcess.renameTo(new File(videoJsonDir, fileContents.get_id() + ".json"));
				} else {
					//keep this id, rename other file
					File toRename = new File(videoJsonDir, id + ".json");
					Video temp = mapper.readValue(toRename, Video.class);
					temp.set_id(UUID.randomUUID().toString());
					mapper.writeValue(new File(videoJsonDir, temp.get_id() + ".json"), temp);
					toRename.delete();
				}

			}

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
