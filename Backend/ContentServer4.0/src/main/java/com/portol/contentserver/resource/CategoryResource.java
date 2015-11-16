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
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
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
import com.mongodb.BasicDBObject;
import com.portol.common.model.Category;
import com.portol.common.model.CategoryAddRequest;
import com.portol.common.model.content.CategoryReference;
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
import com.portol.contentserver.repository.CategoryRepository;
import com.portol.contentserver.repository.MetadataRepository;
import com.portol.contentserver.runnable.BaselineTranscoder;
import com.portol.contentserver.runnable.DBUploader;
import com.portol.contentserver.runnable.Dasher;
import com.portol.contentserver.runnable.MPDMaker;
import com.portol.contentserver.service.MasterAddressGetter;




@Path("/api/v0/category")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {


	final static Logger logger = LoggerFactory.getLogger(CategoryResource.class);

	private CategoryRepository categoryRepo;

	private MetadataRepository metaRepo;


	public CategoryResource(MetadataRepository contentRepo,
			CategoryRepository catRepo) {


		logger.info("in constructor for categoryResource");

		this.categoryRepo = catRepo;
		this.metaRepo = contentRepo;
		
		logger.info("finished constructor for categoryResource");
	}

	@POST
	@Path("/new")
	@Timed
	public Category storeCategory(CategoryAddRequest toAddReq) throws Exception{
	
		if(toAddReq.getNewCat() == null){
			return null;
		}
		
		//part 1, save category
		Category saved = categoryRepo.saveCategory(toAddReq.getNewCat());
		
		
		//part II, update content to point to the new category
		for(String contentKey : toAddReq.getMemberKeys()){
			logger.info("adding content: " + contentKey + " to category: " + saved.getName());
			ContentMetadata matching = metaRepo.getMetadataForParentKey(contentKey);
			if(matching.getMemberOf() == null){
				matching.setMemberOf(new ArrayList<CategoryReference>());
			}
			
			CategoryReference toAdd = new CategoryReference();
			toAdd.setCategoryId(saved.getCategoryId());
			matching.getMemberOf().add(toAdd);
			metaRepo.save(matching);
			
			
		}

		
		return saved;
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
