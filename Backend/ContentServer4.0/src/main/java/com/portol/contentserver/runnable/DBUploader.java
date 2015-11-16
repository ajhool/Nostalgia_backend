package com.portol.contentserver.runnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.portol.common.model.content.Content;
import com.portol.common.model.dash.jaxb.RepresentationType;
import com.portol.contentserver.ContentDataSource;
import com.portol.contentserver.FileMetadata;

public class DBUploader implements Runnable{

	public static final Logger logger = LoggerFactory.getLogger(DBUploader.class);
	private final Dasher mDasher; 
	private final Content matchingContent;
	private final File dashedDataParent;
	private final ContentDataSource contentDataSrc;
	private boolean waitOnPipeline;
	private boolean complete = false;
	private boolean totalSuccess = true;
	
	public DBUploader(Content matching, Dasher dash, File dashedOutputDir, boolean b) throws Exception {
		
		super();
		this.mDasher = dash;
		this.dashedDataParent = dashedOutputDir;
		this.matchingContent = matching;
		this.contentDataSrc = new ContentDataSource(matching.getGlobalSrc());
	this.waitOnPipeline = b;
		
	}

	@Override
	public void run() {
		
		//wait for the dasher to conclude
		//TODO make a real listener rather than crudely polling...
		while(!mDasher.isComplete() && waitOnPipeline){
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		logger.info("Dasher reports completed, beginning the upload process...");
		
		String collName = matchingContent.getGlobalSrc().getCollName();
		
		//Go representation by representation, uploading each in turn
		for(RepresentationType rep : mDasher.getDashedReps()){
			File repDataParent = new File(dashedDataParent, rep.getId());
			
			if(!repDataParent.exists()){
				
				try {
					throw new Exception("error finding data to upload for representation: " + rep.getId());
				} catch (Exception e) {
					logger.error("data for representation: " + rep.getId() + " is not where it was expected...", e);
				}
				continue;
			}
			
			String[] extensions = new String[]{"mp4","m4s"};
			Iterator<File> iter = FileUtils.iterateFiles(repDataParent, extensions, true);
			
			while(iter.hasNext()){
				File toUpload = iter.next();
				
				logger.info("attempting to upload following file: " + toUpload.getName());
				
				String path = repDataParent.getName();
				
				
				
				ArrayList<FileMetadata> metas = new ArrayList<FileMetadata>();
				FileMetadata stringMeta = new FileMetadata("path", path);
				metas.add(stringMeta);
				
				String name = toUpload.getName();
				String ordrString = name.substring(name.indexOf("-") + 1, name.indexOf(".m"));
				
				int order;
				try{
					order = Integer.parseInt(ordrString);


				} catch (NumberFormatException e){
					order = 0;
				}
				

				FileMetadata intMeta = new FileMetadata("order", order);
				metas.add(intMeta);

				FileMetadata repMeta = new FileMetadata("repId", rep.getId());
				metas.add(repMeta);
				
				boolean success = false;
				try {
					success = this.contentDataSrc.add(toUpload, metas, collName);
				} catch (IOException e) {
					
					logger.error("error saving to mongo for file: " + toUpload.getName(), e);
				}
				if(success){
					logger.info("successfully uploaded file: " + toUpload.getName());
					
				} else {
					logger.error("failed to upload file: " + toUpload.getName());
					totalSuccess = false;
				}
			}
			
			if(totalSuccess == false){
				try {
					throw new Exception("upload failed, check server logs");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				totalSuccess = true;
			}
			
		}
		
		complete = true;

		
		
	}

	public boolean isComplete() {
		return complete;
	}

	public boolean isSuccessful(){
		return this.totalSuccess;
	}

}
