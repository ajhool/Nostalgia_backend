package com.portol.contentserver;

import io.dropwizard.setup.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.portol.common.model.content.ContentSource;
import com.portol.contentserver.config.ContentServConfig;
import com.portol.contentserver.healthchecks.MongoHealthCheck;
import com.portol.contentserver.manager.MongoManaged;
import com.portol.contentserver.manager.MongoUnManaged;

public class ContentDataSource {

	private DB sourceDB;
	private GridFS gridFS;
	private MongoUnManaged remoteDataMongo;

	public ContentDataSource(ContentSource globalSrc) throws Exception {
		remoteDataMongo = new MongoUnManaged(globalSrc.getHost(), globalSrc.getPort(), globalSrc.getDbName(), globalSrc.getDbuserName(), globalSrc.getDbPassword());
		this.sourceDB = this.remoteDataMongo.getDB();
		gridFS = new GridFS(sourceDB, globalSrc.getCollName());
	}

	private static final Logger logger = LoggerFactory.getLogger(ContentDataSource.class);
	public boolean add(File toUpload, ArrayList<FileMetadata> metas,
			String collName) throws IOException {

		if(gridFS == null) return false;

		GridFSInputFile in = gridFS.createFile(toUpload);


		GridFSDBFile out = gridFS.findOne(toUpload.getName());
		if(out != null){
			logger.warn(toUpload.getName() + ": file previously in DB");
			if(md5Of(toUpload).equals(in.getMD5())){

				return true;

			} else {
				logger.error("MD5 did not match for file: " + toUpload.getName());
				return false;
			}
		} else {
			logger.info(toUpload.getName() + ": file not in DB");
		}

		for(FileMetadata meta: metas){
			in.put(meta.getName(), meta.getValue());
		}

		in.setContentType("video/MP2T");
		in.save();

		if(md5Of(toUpload).equals(in.getMD5())){

			return true;

		} else {
			logger.error("MD5 did not match for file: " + toUpload.getName());
			return false;
		}

	}

	private synchronized static String md5Of(File saved) throws IOException{
		//verify with MD5


		FileInputStream fis = new FileInputStream(saved);
		String savedmd5 = DigestUtils.md5Hex(fis);
		fis.close();

		return savedmd5;

	}


}
