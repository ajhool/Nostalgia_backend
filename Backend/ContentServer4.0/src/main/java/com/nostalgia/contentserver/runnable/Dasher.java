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

public class Dasher implements Runnable{

	public static final Logger logger = LoggerFactory.getLogger(Dasher.class);
	private File sourceFile;
	private final Content contentInfo;

	private final BaselineTranscoder priorStage;
	private final File output;
	private boolean complete = false;
	private boolean skipPrevious;
	private ArrayList<RepresentationType> targets = new ArrayList<RepresentationType>();

	public Dasher(/*File source,*/  Content data, File output, BaselineTranscoder transcoder, boolean skip){
		super();
		this.contentInfo = data;
		this.output = output;
		this.priorStage = transcoder;
		/*this.sourceFile = source;*/
		this.skipPrevious = skip;

	}


	@Override
	public void run() {

		if(!skipPrevious){
		while(!priorStage.isComplete()){
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sourceFile = priorStage.getOutputFile();
		}
		
		//each representation type needs a separate output
		for(PeriodType period: contentInfo.getMPDInfo().getPeriod()){
			for(AdaptationSetType adapt: period.getAdaptationSet()){
				for(RepresentationType rep : adapt.getRepresentation()){
					logger.info("adding target: " + rep.getId());
					targets.add(rep);
				}
			}
		}

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//now, in targets we should have every representation type that needs to be created
		ArrayList<File> encodedFiles = new ArrayList<File>();
		
		//run ffmpeg for each representation
		//this could take some time...
		for(RepresentationType rep : targets){

			long start = System.currentTimeMillis();
			File existing = new File(output, rep.getId() + ".mp4");
			String fullpath = existing.getAbsolutePath();
			boolean exists = existing.exists();
			if(!exists){
				logger.info("File: " + existing.getPath() + " does not exist, encoding...");
			File encoded = this.encodeWithFFMPEG(rep, sourceFile);
			encodedFiles.add(encoded);
			} else {
				encodedFiles.add(existing);
				logger.info("File: " + existing.getPath() + "exists. Skipping encoding");
			}
			
			long end = System.currentTimeMillis();
			
			Duration thisRun = Duration.ofMillis(end -start);
			logger.info("File for representation: " + rep.getId() + " took " + thisRun.toString() + " to encode to pre-dashing birate");

		}
		
		//delete baseline file, we are done with it
		FileUtils.deleteQuietly(sourceFile);

		//now, we have  a sequence of encoded files
		
		ManualDashFileSet dash = new ManualDashFileSet(output, encodedFiles);
		dash.setVerbose(true);
		try {
			dash.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExitCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		complete = true;
		logger.info("dashing complete. Representations dashed:" );
		for(RepresentationType rep : targets){
			logger.info(rep.getId());
		}

		 
		
		logger.info("ready for upload to DB");
		return;
	}


	public boolean isComplete(){
		return this.complete;
	}

	public ArrayList<RepresentationType> getDashedReps(){
		return this.targets;
	}

	private synchronized File encodeWithFFMPEG(RepresentationType rep, File sourceFile2) {
		FFMPEGController controller = new FFMPEGController();
		
		ArrayList<String> cmds = controller.generateFFMPEGCommand(rep, sourceFile2, output);
		
		ShellCallback sc = null;
		try {
			sc = new StdoutCallback();
		} catch (SecurityException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			int exit = controller.execProcess(cmds, sc, sourceFile.getParentFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new File(output, rep.getId() + ".mp4");
	}

}
