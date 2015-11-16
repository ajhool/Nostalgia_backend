package com.nostalgia.contentserver.runnable;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.castlabs.dash.dashfragmenter.ExitCodeException;
import com.nostalgia.contentserver.FFMPEGController;
import com.nostalgia.contentserver.ShellCallback;
import com.nostalgia.contentserver.StdoutCallback;
import com.nostalgia.contentserver.dash.ManualDashFileSet;
import com.portol.common.model.content.Content;
import com.portol.common.model.dash.jaxb.AdaptationSetType;
import com.portol.common.model.dash.jaxb.PeriodType;
import com.portol.common.model.dash.jaxb.RepresentationType;

public class PipelineScrubber implements Runnable{

	public static final Logger logger = LoggerFactory.getLogger(PipelineScrubber.class);
	private final File contentRoot; 

	private final DBUploader uploader;
	private final MPDMaker mpd;
	
	private boolean complete = false;
	private boolean active = false;
	

	public PipelineScrubber(String uploadRootDir, MPDMaker mpdWaiter, DBUploader dbUL, boolean active){
		super();
		this.active = active;
		contentRoot = new File(uploadRootDir);
		this.uploader = dbUL;
		this.mpd = mpdWaiter;

	}


	@Override
	public void run() {

		if(!active){
			logger.warn("content scrubber deactivated, content @ " + contentRoot.getAbsolutePath() + " will need to be deleted manually" );
			return;
		}
		
		
		while(!uploader.isComplete() || !mpd.isComplete()){
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	
		}
		
		//if there was an error somewhere, don't hose the output
		if(!uploader.isSuccessful()){
			logger.error("uploader failed, skipping deletion");
			return;
		}
		
		
	
		//Delete everything after we have processed it
		if(contentRoot.exists()){
			logger.info("Deleting directory recursively: " + contentRoot.getAbsolutePath());
			try {
				FileUtils.deleteDirectory(contentRoot);
			} catch (IOException e) {
				
				logger.error("Failed to delete intermediate files in dir: " + contentRoot.getAbsolutePath(), e);
			}
		}
		
		complete = true;

		return;
	}


	public boolean isComplete(){
		return this.complete;
	}

	
}
