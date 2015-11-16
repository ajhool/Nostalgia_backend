package com.nostalgia.contentserver.resource;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilderException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.nostalgia.contentserver.model.dash.jaxb.AdaptationSetType;
import com.nostalgia.contentserver.model.dash.jaxb.MPDtype;
import com.nostalgia.contentserver.model.dash.jaxb.RepresentationType;
import com.nostalgia.contentserver.repository.VideoRepository;
import com.nostalgia.contentserver.runnable.BaselineTranscoder;
import com.nostalgia.contentserver.runnable.Dasher;
import com.nostalgia.contentserver.runnable.MPDMaker;
import com.nostalgia.contentserver.runnable.PipelineScrubber;
import com.nostalgia.contentserver.utils.Marshal;
import com.nostalgia.persistence.model.Video;

import io.dropwizard.lifecycle.Managed;


public class AsyncProcessorResource extends AbstractScheduledService implements Managed {

	public static final String FileDataRootDir = "/home/alex/Desktop/Nostalgia_backend/Backend/UserServer/videos";
	final static Logger logger = LoggerFactory.getLogger(AsyncProcessorResource.class);

	private final VideoRepository vidRepo;

	public AsyncProcessorResource(VideoRepository contentRepo) {
		super();
		this.vidRepo = contentRepo;
	}
	

	public Video processFile(Video metaData, File original) throws Exception {

		//next stage: run the video through a baseline transcoding stage in preparation for dashing
		BaselineTranscoder transcoder = new BaselineTranscoder(original, original.getParent(), original.getName() +"_baseline.mp4");

		Thread baselineRunner = new Thread(transcoder);

		baselineRunner.start();



		Dasher dash = new Dasher(metaData, original.getParentFile(), transcoder, false);
		new Thread(dash).start();



		MPDMaker mpdWaiter = new MPDMaker(metaData, dash, original.getParentFile(), true);
		Thread mpdRunner = new Thread(mpdWaiter);
		mpdRunner.start();

		mpdRunner.join();
		
		metaData = mpdWaiter.getUpdatedMetadata();

		PipelineScrubber scrubber = new PipelineScrubber(original.getParentFile(), mpdWaiter, false);
		new Thread(scrubber).start();

		return metaData; 

	}


	private synchronized static String md5Of(File saved) throws IOException{
		//verify with MD5


		FileInputStream fis = new FileInputStream(saved);
		String savedmd5 = DigestUtils.md5Hex(fis);
		fis.close();

		return savedmd5;

	}


	private File saveFile(InputStream uploadedInputStream,
			String serverLocation) {
		File target = new File(serverLocation);
		try {
			OutputStream outputStream = new FileOutputStream(new File(serverLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			logger.error("error saving uploaded file to disk", e);
			return null;
		}

		return target;
	} 

	@Override
	public void start() throws Exception {
		this.startAsync().awaitRunning();
		
	}

	@Override
	public void stop() throws Exception {
		this.stopAsync().awaitTerminated();
		
	}

	private MPDtype getRoughMPD(Video toRoughOut) throws SAXException, IOException, ParserConfigurationException, DatatypeConfigurationException, URISyntaxException{
		
		
		URI mpdRough = getClass().getResource("/template.mpd").toURI();
		
		MPDtype template = Marshal.parseMPD(mpdRough);
		for(AdaptationSetType adapt : template.getPeriod().get(0).getAdaptationSet()){
			for(RepresentationType rep : adapt.getRepresentation()){
				String existing = rep.getId();

				String keyString = "_name_";
				String replaced = existing.replace(keyString,  toRoughOut.get_id());
				rep.setId(replaced);
			}
		}


		return template;
	}
	
	@Override
	protected void runOneIteration() throws Exception {
		HashSet<Video> unprocessed = vidRepo.getVideosWithStatus("METAANDDATA");
		
		if(unprocessed == null){
			logger.info("no videos found to process, sleeping...");
			return; 
		}
		
		
		
		for(Video vid: unprocessed){
			logger.info("processing video with id: " + vid.get_id());
			
			String filePath = FileDataRootDir + "/" + vid.get_id() + "/" + vid.get_id() + ".mp4";
			File contentPieceOrig = new File(filePath);
			
			if(!contentPieceOrig.exists()){
				logger.error("error - no matching file found at: " + contentPieceOrig.getAbsolutePath() + " for video with id: " + vid.get_id());
				continue;
			}
			
			//otherwise, we know it exists
			
			
			
			
			
		}
		
	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 25,
				TimeUnit.SECONDS);
	}

}
