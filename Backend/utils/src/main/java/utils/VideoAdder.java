package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import org.geojson.Feature;
import org.geojson.Point;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;

public class VideoAdder {



	public static void main(String[] args){
		Scanner scanner = new Scanner(System.in);
		boolean running = true;
		File operatingDir = new File(System.getProperty("user.dir"));
		File videoJsonDir = new File(operatingDir, "videos");
		File videoDataDir = new File(operatingDir, "videodata");

		if(!videoJsonDir.exists()){
			videoJsonDir.mkdirs();
		}

		while (running){
			System.out.println("Welcome to the video adder. Scanning for .json files in: " + videoJsonDir.getAbsolutePath() + "...");

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

			System.out.println("\n");
			System.out.print("add which video #?: ");

			int selection = scanner.nextInt(); 

			Video toAdd = scannedVideos.get(selection);
			
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
			
			
		}
		scanner.close();
	}

	private static ArrayList<Video> scanForVideoFilesIn(File videoJsonDir) {
		// set date created where needed and make sure ids are unique
		return null;
	}

	private static void addAlexUser() {

		Point here = null;


		here = new Point(35.9999999, -79.0096901);


		LoginRegisterThread register = new LoginRegisterThread("alex@alex.com", "alex", "alex", "app", true, here, "95bcf68b-ddb7-4e1e-82b6-c61d1f759010");
		register.start();
		try {
			register.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String sessToken;
		String region;

		try {
			sessToken = register.getLoginResponse().getSessionTok();
			region = register.getLoginResponse().getRegion();

		} catch (NullPointerException e) {
			//getLoginResponse might be null;
			e.printStackTrace();
			//Toast.makeText(this.getActivity(), "Can't connect to user database.", Toast.LENGTH_LONG).show();
			System.out.println( "Can't connect to user database.");
		}

	}

	private static void addUSALocation() {
		KnownLocation maine = new KnownLocation();
		maine.setName("USA");
		//	        User uploader = app.getUserRepo().getLoggedInUser();
		Feature object = null;
		try {
			object=   new ObjectMapper().readValue("{\n" +
					"  \"type\": \"Feature\",\n" +
					"  \"properties\": {\n" +
					"    \"name\": \"The USA\",\n" +
					"    \"area\": 6000000\n" +
					"  },\n" +
					"  \"geometry\": {\n" +
					"    \"type\": \"Polygon\",\n" +
					"    \"coordinates\": [\n" +
					"      [\n" +
					"        [-131.484375, 24.5271348225978],\n" +
					"        [-131.484375, 49.83798245308484],\n" +
					"        [-65.390625,  49.83798245308484],\n" +
					"        [ -65.390625, 24.5271348225978]\n" +
					"      ]\n" +
					"    ]\n" +
					"  }\n" +
					"}   ", Feature.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//	        if(uploader == null || object == null){
		//	            String msg = "error - must have logged in user to upload location";
		//	            Log.e(TAG, msg );
		//	            Toast.makeText(MainCaptureActivity.this, msg, Toast.LENGTH_LONG).show();
		//	            return;
		//	        }

		maine.setCreatorId("00000000000000000000000000000000000");

		maine.set_id("2f1e7d7b-564d-4d48-9b4d-e4f556b23ed0");

		maine.setLocation(object);

		KnownLocationCreatorThread creator = new KnownLocationCreatorThread(maine);
		creator.start();
		// result.closeDrawer();
		try {
			creator.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		KnownLocation added = creator.getAdded();

		// Toast.makeText(MainCaptureActivity.this, "location: " + added.getName() + " added successfully", Toast.LENGTH_LONG).show();
		System.out.println("location: " + added.getName() + " added successfully");
		return;

	}

	private static void addNCLocation() {
		KnownLocation maine = new KnownLocation();
		maine.setName("North Caroline");
		//	        User uploader = app.getUserRepo().getLoggedInUser();
		Feature object = null;
		try {
			object=   new ObjectMapper().readValue("{\n" +
					"  \"type\": \"Feature\",\n" +
					"  \"properties\": {\n" +
					"    \"name\": \"North Carolina\",\n" +
					"    \"area\": 60000\n" +
					"  },\n" +
					"  \"geometry\": {\n" +
					"    \"type\": \"Polygon\",\n" +
					"    \"coordinates\": [\n" +
					"      [\n" +
					"        [-84.24316406249999, 34.45221847282654],\n" +
					"        [-84.24316406249999, 36.76529191711624],\n" +
					"        [-75.948486328125,  36.76529191711624],\n" +
					"        [-75.948486328125, 34.45221847282654]\n" +
					"      ]\n" +
					"    ]\n" +
					"  }\n" +
					"}   ", Feature.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//	        if(uploader == null || object == null){
		//	            String msg = "error - must have logged in user to upload location";
		//	            Log.e(TAG, msg );
		//	            Toast.makeText(MainCaptureActivity.this, msg, Toast.LENGTH_LONG).show();
		//	            return;
		//	        }

		maine.setCreatorId("00000000000000000000000000000000000");

		maine.set_id("f5852e4e-1f65-4d31-b2e0-3ac453e8b1ba");

		maine.setLocation(object);

		KnownLocationCreatorThread creator = new KnownLocationCreatorThread(maine);
		creator.start();
		// result.closeDrawer();
		try {
			creator.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		KnownLocation added = creator.getAdded();

		// Toast.makeText(MainCaptureActivity.this, "location: " + added.getName() + " added successfully", Toast.LENGTH_LONG).show();
		System.out.println("location: " + added.getName() + " added successfully");

	}

	private static void addNALocation() {
		KnownLocation maine = new KnownLocation();
		maine.setName("North America");
		//	        User uploader = app.getUserRepo().getLoggedInUser();
		Feature object = null;
		try {
			object=   new ObjectMapper().readValue("{\n" +
					"  \"type\": \"Feature\",\n" +
					"  \"properties\": {\n" +
					"    \"name\": \"North America\",\n" +
					"    \"area\": 60000000\n" +
					"  },\n" +
					"  \"geometry\": {\n" +
					"    \"type\": \"Polygon\",\n" +
					"    \"coordinates\": [\n" +
					"      [\n" +
					"        [-170.15625, 16.46769474828897],\n" +
					"        [-170.15625, 77.57995914400348],\n" +
					"        [ -54.4921875,  77.57995914400348],\n" +
					"        [ -54.4921875, 16.46769474828897]\n" +
					"      ]\n" +
					"    ]\n" +
					"  }\n" +
					"}   ", Feature.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//	        if(uploader == null || object == null){
		//	            String msg = "error - must have logged in user to upload location";
		//	            Log.e(TAG, msg );
		//	            Toast.makeText(MainCaptureActivity.this, msg, Toast.LENGTH_LONG).show();
		//	            return;
		//	        }

		maine.setCreatorId("00000000000000000000000000000000000");

		maine.set_id("5bfb7ea5-54a2-483d-a049-44dc76bb06ca");

		maine.setLocation(object);

		KnownLocationCreatorThread creator = new KnownLocationCreatorThread(maine);
		creator.start();
		// result.closeDrawer();
		try {
			creator.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		KnownLocation added = creator.getAdded();

		// Toast.makeText(MainCaptureActivity.this, "location: " + added.getName() + " added successfully", Toast.LENGTH_LONG).show();
		System.out.println("location: " + added.getName() + " added successfully");

	}

}
