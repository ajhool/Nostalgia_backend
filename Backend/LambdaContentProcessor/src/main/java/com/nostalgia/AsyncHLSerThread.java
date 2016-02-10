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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.nostalgia.contentserver.runnable.BaselineTranscoder;
import com.nostalgia.contentserver.runnable.HLSer;


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

		try {
			baselineRunner.join();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}


		ArrayList<String> reses = new ArrayList<String>();
		reses.add("320x180");
		HLSer dash = new HLSer(reses, workingDir, transcoder, false);
		Thread hlsExec = new Thread(dash);
		hlsExec.start();

		try {
			baselineRunner.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			hlsExec.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//delete baseline file, we are done with it
		FileUtils.deleteQuietly(transcoder.getOutputFile());
		
		return; 

	}


}
