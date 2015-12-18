package com.nostalgia.aws;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jets3t.service.CloudFrontService;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.EncryptionUtil;
import org.jets3t.service.utils.ServiceUtils;

import com.amazonaws.services.s3.transfer.TransferManager;
import com.google.common.io.Resources;

public class SignedCookieCreator {

	public final AWSConfig config; 
	
    
	public SignedCookieCreator(AWSConfig config){
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		this.config = config;
	}

//	//see http://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-setting-signed-cookie-canned-policy.html#private-content-canned-policy-statement-cookies-values
//	final String cannedPolicy = "" +
//	"{ "+
//		  "\"Statement\": [ "+
//		     "{" +
//		        "\"Resource\":\"<BaseURL>\"," + 
//		         "\"Condition\":{" +
////		         "\"IpAddress\":{\"AWS:SourceIp\":\"192.0.2.0/24\"}," + 
//		         "\"DateLessThan\":{\"AWS:EpochTime\":<ExpirationTime>}" +
//		         "}" +
//		     "}" +
//		   "]" + 
//		"}";

	public Map<String, String> generateCookies(String baseUrl, long expirationTime) throws Exception {

		HashMap<String, String> cookies = new HashMap<String, String>();
		
		String customPolicy = CloudFrontService.buildPolicyForSignedUrl(baseUrl, new Date(expirationTime), "0.0.0.0/0", null);
		
		cookies.put("CloudFront-Policy", ServiceUtils.toBase64(customPolicy.getBytes()));
		byte[] signatureBytes = EncryptionUtil.signWithRsaSha1(getDerPrivateKey(), customPolicy.getBytes("UTF-8"));
		String signature = ServiceUtils.toBase64(signatureBytes).replace('+', '-').replace('=', '_').replace('/', '~');
		
		cookies.put("CloudFront-Signature", signature);

		cookies.put("CloudFront-Key-Pair-Id", config.keyPairId);
		cookies.put("original", customPolicy);
		return cookies; 
	}

	// Convert your DER file into a byte array.
	private byte[] getDerPrivateKey() throws FileNotFoundException, IOException {
		URL icon = getClass().getResource("/private.der");
		byte[] defaultEncoded = Resources.toByteArray(icon);
		return defaultEncoded; 
	}




}
