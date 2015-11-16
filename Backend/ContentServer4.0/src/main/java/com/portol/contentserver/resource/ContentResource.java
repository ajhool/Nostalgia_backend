package com.portol.contentserver.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.portol.common.model.MovieFact;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.Content.Type;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.content.ContentSource;
import com.portol.common.model.dash.jaxb.AdaptationSetType;
import com.portol.common.model.dash.jaxb.MPDtype;
import com.portol.common.model.dash.jaxb.RepresentationType;
import com.portol.common.model.payment.Payment;
import com.portol.contentserver.config.MongoConfig;
import com.portol.contentserver.manager.MongoManaged;
import com.portol.contentserver.repository.ContentRepository;
import com.portol.contentserver.repository.MetadataRepository;
import com.portol.contentserver.runnable.BaselineTranscoder;
import com.portol.contentserver.runnable.DBUploader;
import com.portol.contentserver.runnable.Dasher;
import com.portol.contentserver.runnable.MPDMaker;
import com.portol.contentserver.runnable.PipelineScrubber;
import com.portol.contentserver.service.MasterAddressGetter;




@Path("/api/v0/addcontent")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContentResource {

	public static final String apiKey_payment = "foo";
	public static final String FileDataWorkingDirectory = "Uploads";
	public static final int playerport = 8080;
	public static final int paymentport = 9090;
	final static Logger logger = LoggerFactory.getLogger(ContentResource.class);

	private final MongoConfig toUploadTo;
	private final ContentRepository contentRepo;
	private final MetadataRepository metaRepo;

	public ContentResource(ContentRepository contentRepo, MetadataRepository metaRepo,  MongoConfig toUploadTo) {

		this.contentRepo = contentRepo;
		this.metaRepo = metaRepo;
		logger.info("in constructor for contentresource");
		this.toUploadTo = toUploadTo;
		logger.info("finished constructor for contentresource");
	}

	@POST
	@Path("/basic")
	@Timed
	public Content storeContent(Content toAdd) throws Exception{
		//who know what form this will take. Likely the encoding will be done here 
		//so a video file will come in

		//instead, we are going to ignore the file for now and assume allnew content will be uploaded by an application 
		//using SFTP

		//TODO
		//boolean valid = validateMeta(toAdd);
		//
		//		if(!valid){
		//			//blacklist?
		//			return null;
		//		}

		Content existing = contentRepo.findByVideoKey(toAdd.getContentKey());
		if(existing != null){
			resp.sendError(403, "Content with key: " + toAdd.getContentKey() + "already exists!");
			return null;
		}


		ContentSource newSrc = null;
		//find a database taking additional data 
		if(toAdd.getType() == Type.VOD){
			logger.info("VOD content addition detected, storing in the following DB: " );
			newSrc = new ContentSource();

			logger.info("setting source to be: " + this.toUploadTo.host);
			newSrc.setHost(this.toUploadTo.host);

			logger.info("setting port to be: " + this.toUploadTo.port);
			newSrc.setPort(this.toUploadTo.port);

			logger.info("setting DB to open to be: " + this.toUploadTo.db);
			newSrc.setDbName(this.toUploadTo.db);

			newSrc.setCollName(toAdd.getContentKey());

			logger.info("setting DB user to be: " + this.toUploadTo.user);
			newSrc.setDbuserName(this.toUploadTo.user);

			newSrc.setDbPassword(this.toUploadTo.password);


		}

		if(newSrc != null){
			toAdd.setGlobalSrc(newSrc);
		}

		//obtain UID for this new content
		ContentMetadata child = null;
		if(toAdd.getMetaData() != null){
			ContentMetadata splashReply = metaRepo.save(toAdd.getMetaData());

			//dump the screencontents to save space, since we already have it stored in a repo
			toAdd.setMetaData(null);
			child = splashReply;

			toAdd.setSplashDataId(splashReply.getMetadataId());
		}

		Content reply = contentRepo.save(toAdd);
		String parent = reply.getId();
		toAdd.setCreated(new Date(System.currentTimeMillis()));
		toAdd.setFreeSeconds(300);
		toAdd.setStatus(Content.Status.AVAILABLE);

		//add a basic MPD file on
		toAdd.setMPDInfo(this.getRoughMPD(toAdd));

		Content  result = contentRepo.save(toAdd);

		if(child != null){
			child.setParentContentId(parent);
			child.setParentContentKey(toAdd.getContentKey());
			metaRepo.save(child);
		}

		Content sanitized = sanitizeContent(result);

		return sanitized;
	}


	private Content sanitizeContent(Content result) {
		result.setMPDInfo(null);
		result.setPreviewMPD(null);
		result.setGlobalSrc(null);

		return result;
	}

	private MPDtype getRoughMPD(Content toRoughOut) throws SAXException, IOException, ParserConfigurationException, DatatypeConfigurationException{
		MPDtype template = com.portol.common.utils.Marshal.parseMPD("template.mpd");
		for(AdaptationSetType adapt : template.getPeriod().get(0).getAdaptationSet()){
			for(RepresentationType rep : adapt.getRepresentation()){
				String existing = rep.getId();

				String keyString = "_name_";
				String replaced = existing.replace(keyString,  toRoughOut.getContentKey());
				rep.setId(replaced);
			}
		}


		return template;
	}


	@Context HttpServletResponse resp; 


	@POST
	@Path("/moviefax")
	@Timed
	public List<MovieFact> setFax(ArrayList<MovieFact> toSet, @QueryParam("targetContentId") String contentId) throws IOException{
		Content toAddFax = contentRepo.findById(contentId);
		if(toAddFax == null){
			resp.sendError(404, "invalid content id number specified");
			return null;
		}
		Collections.sort(toSet);
		toAddFax.setMovieFax(toSet);
		contentRepo.save(toAddFax);
		return toSet;
	}

	@DELETE
	@Path("/metadata/{contentKey}")
	@Timed
	public ContentMetadata deleteMetadata(@PathParam("contentKey") String contentKey) throws Exception{

		//find content
		Content parent = contentRepo.findByVideoKey(contentKey);

		String metadataId = parent.getSplashDataId();

		if(metadataId == null){
			return null;
		}

		ContentMetadata deleted = metaRepo.getSplashScreenById(metadataId);

		parent.setSplashDataId(null);
		parent.setMetaData(null);

		metaRepo.remove(deleted);

		contentRepo.save(parent);

		return deleted;


	}


	@POST
	@Path("/metadata")
	@Timed
	public ContentMetadata storeContentMetadata(ContentMetadata toAdd) throws Exception{

		if(toAdd.getParentContentKey() == null || toAdd.getParentContentKey().length() < 3){
			resp.sendError(404, "must specify video key to add metadata to");
			return null;
		}
		
		if(toAdd.getPrices() == null){
			resp.sendError(404, "must provide pricing information");
			return null;
		}

		//step 1 locate matching content
		Content parent = contentRepo.findByVideoKey(toAdd.getParentContentKey());

		if(parent == null){
			resp.sendError(404, "no content matching this metadata found");
			return null;
		}

		//step 2: check for exisiting metadata
		ContentMetadata existingMeta = null;
		if(parent.getSplashDataId() != null){
			existingMeta = metaRepo.getSplashScreenById(parent.getSplashDataId());
			resp.sendError(204, "content already has metadata. Please use delete api call before adding new metadata.");
			return existingMeta; 
		}

		//step 3: add metadata

		ContentMetadata splashReply = metaRepo.save(toAdd);

		//dump the screencontents to save space, since we already have it stored in a repo
		parent.setMetaData(null);
		parent.setSplashDataId(splashReply.getMetadataId());


		//add references to child
		splashReply.setParentContentId(parent.getId());
		splashReply.setParentContentKey(parent.getContentKey());
		splashReply.setSecondsFree(parent.getFreeSeconds());
		


		Content reply = contentRepo.save(parent);


		ContentMetadata result = metaRepo.save(splashReply);


		return result;
	}

	@POST
	@Path("/upload/multipart/{contentKey}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public Response uploadFile(@FormDataParam("mainfile") InputStream fis,
			@Context HttpServletRequest a_request,
			//			@FormDataParam("file") Optional<FormDataContentDisposition> name,
			@FormDataParam("mainfile") FormDataContentDisposition fileNameForm,
			@QueryParam("checksum") Optional<String> checksum,
			@PathParam("contentKey") String contentKey) throws Exception {

		//check that we have matching content metadata so that we know what to do with the file
		Content matching = contentRepo.findByVideoKey(contentKey);

		if(matching == null){
			return Response.status(404).entity("no metadata found for uploaded file ID").build();
		}

		//if we have a checksum
		if(checksum.orNull() != null){


			if(checksum.orNull().equalsIgnoreCase("")){
				return Response.status(412).entity("Must provide MD5 for uploaded file").build();
			}


		}


		String fileName = fileNameForm.getFileName();


		if(fileName == null){
			return Response.status(412).entity("Must provide name for uploaded file in form").build();
		}

		String filePath = FileDataWorkingDirectory + "/" + matching.getContentKey();
		File contentPieceWorkingDir = new File(filePath);
		FileUtils.forceMkdir(contentPieceWorkingDir);


		String savedFilePath = filePath + "/" + fileName;
		File original = new File(savedFilePath);


		boolean isMatch = false;
		String savedmd5 = null;
		if(original.exists() && checksum.orNull() != null){
			//check MD5
			savedmd5 = this.md5Of(original);
			//if they match, then skip the upload process
			if(savedmd5.equalsIgnoreCase(checksum.orNull())){
				isMatch = true;
				fis.close();
				logger.info("exact checksum match found, skipping upload");
			}
		} 


		if(!isMatch){
			//wipe out whatever was there
			if(original.exists()){
				FileUtils.forceDelete(original);
			}

			//re-upload it
			long start = System.currentTimeMillis();
			original = saveFile(fis, savedFilePath);
			long end = System.currentTimeMillis();

			Duration thisRun = Duration.ofMillis(end -start);

			long speed = (original.length() * 8) / (((end - start) / 1000) * (long)Math.pow(2, 20));

			logger.info("File: " + original.getName() + " took " + thisRun.toString() + " to download at a speed of " + speed + "mbps");
		}




		if(original == null || !original.exists()){
			logger.error("no file saved!");
			return Response.status(500).build();
		}

		//otherwise, assume file exists and is legit

		if(checksum.orNull() != null){
			savedmd5 = this.md5Of(original);

			if(!savedmd5.equalsIgnoreCase(checksum.orNull())){
				//MD5 failure
				logger.error("file upload failed MD5 verification");
				FileUtils.forceDelete(original);
				return Response.status(400).build();
			}
		}

		//we have a good file if we made it here

		//next stage: run the video through a baseline transcoding stage in preparation for dashing
		BaselineTranscoder transcoder = new BaselineTranscoder(original, filePath, fileName +"_baseline.mp4");

		Thread baselineRunner = new Thread(transcoder);

		baselineRunner.start();


		//start ze dashing process
		File dashedOutputDir = new File(contentPieceWorkingDir, matching.getGlobalSrc().getCollName());

		FileUtils.forceMkdir(dashedOutputDir);

		Dasher dash = new Dasher(/*original, */matching, dashedOutputDir, transcoder, false);
		new Thread(dash).start();

		//add listener to upload to DB after dashing complete
		DBUploader dbUL = new DBUploader(matching, dash, dashedOutputDir, true);
		new Thread(dbUL).start();

		MPDMaker mpdWaiter = new MPDMaker(matching, dash, contentRepo, true);
		new Thread(mpdWaiter).start();


		PipelineScrubber scrubber = new PipelineScrubber(filePath, mpdWaiter, dbUL, true);
		new Thread(scrubber).start();


		return Response.status(200).build();
	}


	@Path("/upload/{contentKey}")
	@POST
	@Produces("application/json")
	@Consumes("*/*")
	public Response uploadFile(
			final InputStream fileInputStream,
			@Context HttpServletRequest a_request,
			@PathParam("contentKey") String contentKey,
			@QueryParam("checksum") String checksum, 
			@QueryParam("flowFilename") Optional<String> filename) throws Exception {

		//check that we have matching content metadata so that we know what to do with the file
		Content matching = contentRepo.findByVideoKey(contentKey);

		if(matching == null){
			return Response.status(404).entity("no metadata found for uploaded file ID").build();
		}

		if(checksum == null || checksum.equalsIgnoreCase("")){
			return Response.status(412).entity("Must provide MD5 for uploaded file").build();
		}


		String rawHeader = a_request.getHeader("Content-Disposition");
		String fileName = null;
		if(rawHeader == null){
			fileName = filename.orNull(); 


		} else {

			try {
				fileName = rawHeader.substring(rawHeader.lastIndexOf("=\"") + 2, rawHeader.length()-1);
			} catch (Exception e){
				logger.error("bad content disposition header");
			}

		}

		if(fileName == null){
			return Response.status(412).entity("Must provide name for uploaded file in header or requestparam").build();
		}

		String filePath = FileDataWorkingDirectory + "/" + matching.getContentKey();
		File contentPieceWorkingDir = new File(filePath);
		FileUtils.forceMkdir(contentPieceWorkingDir);


		String savedFilePath = filePath + "/" + fileName;
		File original = new File(savedFilePath);




		boolean isMatch = false;
		String savedmd5 = null;
		if(original.exists()){
			//check MD5
			savedmd5 = this.md5Of(original);
			//if they match, then skip the upload process
			if(savedmd5.equalsIgnoreCase(checksum)){
				isMatch = true;
				fileInputStream.close();
				logger.info("exact checksum match found, skipping upload");
			}
		} 

		if(!isMatch){
			//wipe out whatever was there
			if(original.exists()){
				FileUtils.forceDelete(original);
			}

			//re-upload it
			long start = System.currentTimeMillis();
			original = saveFile(fileInputStream, savedFilePath);
			long end = System.currentTimeMillis();

			Duration thisRun = Duration.ofMillis(end -start);

			long speed = (original.length() * 8) / (((end - start) / 1000) * (long)Math.pow(2, 20));

			logger.info("File: " + original.getName() + " took " + thisRun.toString() + " to download at a speed of " + speed + "mbps");
		}




		if(original == null || !original.exists()){
			logger.error("no file saved!");
			return Response.status(500).build();
		}

		//otherwise, assume file exists and is legit

		savedmd5 = this.md5Of(original);

		if(!savedmd5.equalsIgnoreCase(checksum)){
			//MD5 failure
			logger.error("file upload failed MD5 verification");
			FileUtils.forceDelete(original);
			return Response.status(400).build();
		}

		//we have a good file if we made it here


		//next stage: run the video through a baseline transcoding stage in preparation for dashing
		BaselineTranscoder transcoder = new BaselineTranscoder(original, filePath, fileName +"_baseline.mp4");

		Thread baselineRunner = new Thread(transcoder);

		baselineRunner.start();


		//start ze dashing process
		File dashedOutputDir = new File(contentPieceWorkingDir, matching.getGlobalSrc().getCollName());

		FileUtils.forceMkdir(dashedOutputDir);

		Dasher dash = new Dasher(/*original, */matching, dashedOutputDir, transcoder, false);
		new Thread(dash).start();

		//add listener to upload to DB after dashing complete
		DBUploader dbUL = new DBUploader(matching, dash, dashedOutputDir, true);
		new Thread(dbUL).start();

		MPDMaker mpdWaiter = new MPDMaker(matching, dash, contentRepo, true);
		new Thread(mpdWaiter).start();


		PipelineScrubber scrubber = new PipelineScrubber(filePath, mpdWaiter, dbUL, true);
		new Thread(scrubber).start();

		return Response.status(200).build();

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

	private boolean validateMeta(Content metadataToValidate){
		//TODO
		//sanity checks
		return true;
	}

}
