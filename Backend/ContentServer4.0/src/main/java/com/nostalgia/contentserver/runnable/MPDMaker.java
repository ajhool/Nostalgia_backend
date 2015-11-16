package com.nostalgia.contentserver.runnable;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.castlabs.dash.dashfragmenter.ExitCodeException;
import com.nostalgia.contentserver.FFMPEGController;
import com.nostalgia.contentserver.ShellCallback;
import com.nostalgia.contentserver.StdoutCallback;
import com.nostalgia.contentserver.dash.ManualDashFileSet;

public class MPDMaker implements Runnable{

	public static final Logger logger = LoggerFactory.getLogger(MPDMaker.class);

	private  Content needMPD;
	private  Dasher waitingOn;
	private ContentRepository contentRepo;
	private boolean wait = true;
	private boolean complete = false; 


	public MPDMaker(Content needMPD, Dasher waitingOn,
			ContentRepository contentRepo2, boolean b) {
		super();
		this.needMPD = needMPD;
		this.waitingOn = waitingOn;
		this.contentRepo = contentRepo2;
		this.wait = b;
	}


	@Override
	public void run() {

		//wait for the dasher to conclude
				//TODO make a real listener rather than crudely polling...
				while(!waitingOn.isComplete() && wait){
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				logger.info("Dasher reports completed, creating accurate mpd file...");
				//update content
				Content needsMPD = contentRepo.findById(needMPD.getId());

				MPDtype mpd = null;
				try {
					mpd = this.getMPDFor(needsMPD);
					needsMPD.setMPDInfo(mpd);
				} catch (SAXException | IOException
						| ParserConfigurationException
						| DatatypeConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Date now = new Date();
				long duration = (long) (((double)mpd.getMediaPresentationDuration().getTimeInMillis(now)) / (double)1000.00);
				needsMPD.setLengthInSec((int)duration);
				logger.info("length in sec set to be: " + duration + " from XML duration: " + mpd.getMediaPresentationDuration());
				
				if(needsMPD != null && needsMPD.getPreviewMPD() == null){
					//TODO remove this to the content upload? 
					try {
						needsMPD.regeneratePreviewMPD();
					} catch (Exception e) {
						logger.error("error generating preview MPD", e);
					}
				}
				
				contentRepo.save(needsMPD);
				setComplete(true);
				
	}


	private MPDtype getMPDFor(Content toMake) throws SAXException, IOException, ParserConfigurationException, DatatypeConfigurationException{
		//MPDtype template = Marshal.parseMPD("template.mpd");
		//MPDtype template = toMake.getMPDInfo();
		MPDtype generated = Marshal.parseMPD("Uploads/" + toMake.getContentKey() + "/" + toMake.getContentKey() +"/Manifest.mpd");



		//template.setMediaPresentationDuration(generated.getMediaPresentationDuration());
		//template.getPeriod().get(0).setDuration(generated.getPeriod().get(0).getDuration());

		

		return generated;


	}


	public boolean isComplete() {
		return complete;
	}


	private void setComplete(boolean complete) {
		this.complete = complete;
	}

}
