package com.nostalgia;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class ContentProcessFunction implements RequestHandler<Object, Object> {

    @Override
    public Object handleRequest(Object input, Context context) {
        context.getLogger().log("Input: " + input);
        
        String contentId = input.toString(); 
        S3UlDlClient s3Cli = null;
        File parentSaveDir = new File("/tmp");
        try {
			 s3Cli = new S3UlDlClient(new S3Config());
		} catch (Exception e) {
			System.err.println("failed instantiation of s3 client\n" + e);
		} 

        //get corresponding file from pending folder
        File tempDir = s3Cli.getDirFromPending(contentId, parentSaveDir); 
        
        
        //call HLSer
        //call thumbnailer
        
        //upload to data folder
        
        //on successful upload, delete corresponding folder in pending
        
        //delete temp file
        FileUtils.deleteQuietly(tempDir);

        
        return null;
    }

}
