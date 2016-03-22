package com.nostalgia;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.persistence.model.ProcessContentRequestPojo;
import com.nostalgia.persistence.model.ProcessContentResponsePojo;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.VideoTranscodeCallbackPojo;

public class EmailConfirmFunction {

	private static class Waiter extends Thread {
		@Override
		public void run(){
			try {
				Thread.sleep(80 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void handler(InputStream inputStream, OutputStream outputStream, Context context) throws IOException{
		ObjectMapper mapper = new ObjectMapper(); 
		long start = System.currentTimeMillis(); 
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "UTF-8");
		String theString = writer.toString();

		try {
			context.getLogger().log("Input: " + mapper.writeValueAsString(theString));
		} catch (Exception e){
			e.printStackTrace();
		}

		User user = null; 

		//simulate lookup work
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		String response = null;

		if(user == null){
			response = "Error - invalid expired confirmation link!";
		} else {
			response = "Success - thanks for validating your email"; 
		}

		System.out.println("Writing output to outputStream at relative time: " + (System.currentTimeMillis() - start) + " ms into function");

		outputStream.write(response.getBytes("UTF-8"));
		outputStream.flush();
		outputStream.close();

		System.out.println("waiting 80 sec while thread runs");
		Waiter wait = new Waiter();
		wait.start();
		try {
			wait.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("waiter joined successfully at relative time: " + (System.currentTimeMillis() - start) + " ms into function");
		System.out.println("quitting...");
		System.out.println("function measured executon time as: " + ((double)(System.currentTimeMillis() - start) / (double) 1000) + "seconds");
		return; 
	}

}
