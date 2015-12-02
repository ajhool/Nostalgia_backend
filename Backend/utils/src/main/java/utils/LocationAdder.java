package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.geojson.Feature;
import org.geojson.Point;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;

public class LocationAdder {

	private static void addRTLocation()  {
		KnownLocation duke = new KnownLocation();
		duke.setName("Research Triangle");
		//	        User uploader = app.getUserRepo().getLoggedInUser();
		Feature object = null;
		try {
			object=   new ObjectMapper().readValue("{\n" +
					"  \"type\": \"Feature\",\n" +
					"  \"properties\": {\n" +
					"    \"name\": \"Research Triangle\",\n" +
					"    \"area\": 252\n" +
					"  },\n" +
					"  \"geometry\": {\n" +
					"    \"type\": \"Polygon\",\n" +
					"    \"coordinates\": [\n" +
					"      [\n" +
					"        [-87.83, 40.05],\n" +
					"        [-89.67, 40.78],\n" +
					"        [-70.24, 33.90],\n" +
					"        [-90.93, 33.00]\n" +
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

		duke.setCreatorId("000000000000000000");



		duke.setLocation(object);
		duke.set_id("756e4e45-5aee-4e43-ad28-f7c7c4f41d0d");

		KnownLocationCreatorThread creator = new KnownLocationCreatorThread(duke);
		creator.start();
		//	        result.closeDrawer();
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

	private static void addMaineLocation()  {
		KnownLocation maine = new KnownLocation();
		maine.setName("Maine");
		//	        User uploader = app.getUserRepo().getLoggedInUser();
		Feature object = null;
		try {
			object=   new ObjectMapper().readValue("{\n" +
					"  \"type\": \"Feature\",\n" +
					"  \"properties\": {\n" +
					"    \"name\": \"Maine\",\n" +
					"    \"area\": 6000\n" +
					"  },\n" +
					"  \"geometry\": {\n" +
					"    \"type\": \"Polygon\",\n" +
					"    \"coordinates\": [\n" +
					"      [\n" +
					"        [-70.828857421875, 43.50872101129684],\n" +
					"        [-70.828857421875, 47.05515408550348],\n" +
					"        [-67.576904296875, 47.05515408550348],\n" +
					"        [-67.576904296875, 43.50872101129684]\n" +
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

		maine.set_id("234c5080-28f9-47b4-bfea-37587e7d3054");

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

	public static void main(String[] args){
		Scanner scanner = new Scanner(System.in);
		boolean running = true;
		while (running){
			System.out.print("Enter your command: ");
			
			String command = scanner.nextLine();

			switch(command){
			case("addresearchtriangle"):{
				addRTLocation();
			}
			break;
			case("addmaine"):{
				addMaineLocation();
			}
			break;
			case("addusa"):{
				addUSALocation();
			}
			break;
			case("addnorthcarolina"):{
				addNCLocation();
			}
			break;
			case("addnorthamerica"):{
				addNALocation();
			}
			break;
			case("createalexuser"):{
				addAlexUser();
			}
			break;
			case("exit"):
			case("quit"):
			case("q"):
				running = false;
			break;
			default:
				System.out.println("Command: " + command + " not recognized");

			}

			


		}
		scanner.close();
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
