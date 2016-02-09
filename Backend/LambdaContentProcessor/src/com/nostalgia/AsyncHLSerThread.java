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
import com.nostalgia.contentserver.config.DataConfig;
import com.nostalgia.contentserver.model.dash.jaxb.AdaptationSetType;
import com.nostalgia.contentserver.model.dash.jaxb.MPDtype;
import com.nostalgia.contentserver.model.dash.jaxb.RepresentationType;
import com.nostalgia.contentserver.repository.VideoRepository;
import com.nostalgia.contentserver.runnable.BaselineTranscoder;
import com.nostalgia.contentserver.runnable.HLSer;
import com.nostalgia.contentserver.runnable.MPDMaker;
import com.nostalgia.contentserver.runnable.PipelineScrubber;
import com.nostalgia.contentserver.utils.Marshal;
import com.nostalgia.persistence.model.Video;

import io.dropwizard.lifecycle.Managed;


public class AsyncHLSerThread extends Thread {

	private final TranscodeConfig config;
	private final File target;
	private final File workingDir;

	public AsyncHLSerThread(TranscodeConfig transConfig, File workingDir, File targetFile) {
		super();
		this.config = transConfig; 
		this.workingDir = workingDir;
		this.target = targetFile; 
	}


	@Override
	public void run() {

		//next stage: run the video through a baseline transcoding stage in preparation for dashing
		File baseline = new File(target.getName() +"_baseline.mp4");

		BaselineTranscoder transcoder = new BaselineTranscoder(target, workingDir, baseline.toString());

		Thread baselineRunner = new Thread(transcoder);

		baselineRunner.start();

		baselineRunner.join();


		ArrayList<String> reses = new ArrayList<String>();
		reses.add("320x180");
		HLSer dash = new HLSer(reses, workingDir, transcoder, false);
		Thread hlsExec = new Thread(dash);
		hlsExec.start();

		baselineRunner.join();
		hlsExec.join();

		//delete baseline file, we are done with it
		FileUtils.deleteQuietly(transcoder.getOutputFile());
		
		return; 

	}


}
