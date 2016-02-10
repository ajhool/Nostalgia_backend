package com.nostalgia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.nostalgia.contentserver.runnable.ThumbnailMaker;

public class AsyncThumbnailThread extends Thread {


	private boolean running = false;
	private final File workingDir;
	private final File sourceFile;
	private final ThumbnailConfig config; 
	private final ArrayList<String> thumbs = new ArrayList<String>(); 
	public ArrayList<String> getThumbs() {
		return thumbs;
	}


	public AsyncThumbnailThread(ThumbnailConfig config, File workingDir, File thumbnailSource) {
		super();
		this.config = config; 
		this.workingDir = workingDir;
		this.sourceFile = thumbnailSource;
	}


	private List<File> processFile(String id, File original, File thumbnailParentDir) throws Exception {

		ThumbnailMaker maker = new ThumbnailMaker(id, original, thumbnailParentDir);

		Thread runner = new Thread(maker);

		runner.start();
		runner.join();


		return maker.getOutputFiles(); 

	}



	@Override
	public void run() {

			try { 

				System.out.println("generating thumbs video with id: " + sourceFile.getName());

				if(!sourceFile.exists()){
					System.err.println("error - no matching file found at: " + sourceFile.getAbsolutePath() + " for video");
					return;
				}

				//generate dir for thumbs
				File thumbnailParentDir = new File(workingDir, "thumbnails");
				thumbnailParentDir.mkdirs(); 

				//otherwise, we know it exists

				List<File> result = null;
				try {
					result = processFile(sourceFile.getName(), sourceFile, thumbnailParentDir);
				} catch (Exception e) {
					System.err.println("Error processing thumbs: " + e);
					return; 
				}

				for(File thumb : result){
					thumbs.add( workingDir.getName() + "/" + thumbnailParentDir.getName() + "/" + thumb.getName());
				}


			} finally {
				running = false;
			}
		
		return; 

	}
	
	
}
