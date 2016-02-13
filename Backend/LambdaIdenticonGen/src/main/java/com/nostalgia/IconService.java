package com.nostalgia;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


import org.apache.commons.codec.binary.Base64;



import com.fasterxml.jackson.databind.ObjectMapper;

import com.nostalgia.IconServiceConfig;
import com.nostalgia.identicon.IdenticonUtil;
import com.nostalgia.persistence.model.icon.IconReply;



public class IconService {

	private final IconServiceConfig conf;

	private static final ObjectMapper mapper = new ObjectMapper();
	IdenticonUtil generator = new IdenticonUtil();
	
	public IconService(IconServiceConfig conf){
		this.conf = conf;
	
	}
	
	
	
	public String getBase64Icon(String key) throws Exception{

		
//		UriBuilder uribuild = UriBuilder.fromUri(conf.iconHost + ":" + conf.port + conf.newIconPath + "?key=bar");
//		
//		IconReply resp = null;
//		
//		try{
//			resp = icComm.target(uribuild).request().post(Entity.text(key), IconReply.class);
//	} catch (Exception e){
//		logger.error("error retrieving icon from icon microservice. Using default Icon instead", e);
//		 resp = new IconReply();
//		resp.setUsingDefault(true);
//		
//		URL icon = getClass().getResource("/local/defaultUserIcon.png");
//		byte[] defaultEncoded = Resources.toByteArray(icon);
//		resp.setEncodedImage(defaultEncoded);
//		
//	}
		
		byte[] result = generator.makeIdenticon(key);
		return  new String(Base64.encodeBase64(result, false, false));
	}
	
	
}
