package awstest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.Security;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.utils.ServiceUtils;

import com.google.common.io.Resources;
import com.nostalgia.aws.AWSConfig;
import com.nostalgia.aws.SignedCookieCreator;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		SignedCookieCreator create = new SignedCookieCreator(new AWSConfig());

		java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
		java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);

		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
		long time = 1451066974000L; 
		//call to aws here if needed for new tokens
//		Map<String, String> generated = create.generateCookies("https://d1natzk16yc4os.cloudfront.net/79bd3570-6f11-48ef-ad61-3c3ecc957752/thumbnails/79bd3570-6f11-48ef-ad61-3c3ecc957752_thumbnail_04.jpg"/*"https://d1natzk16yc4os.cloudfront.net/*"*/, time);
//		
		Map<String, String> generated = create.generateCookies("https://d1natzk16yc4os.cloudfront.net/*"/*"https://d1natzk16yc4os.cloudfront.net/*"*/, time);
		
		 HttpClientContext context = HttpClientContext.create();
    
		//HttpGet get = new HttpGet("https://d1natzk16yc4os.cloudfront.net/79bd3570-6f11-48ef-ad61-3c3ecc957752/thumbnails/79bd3570-6f11-48ef-ad61-3c3ecc957752_thumbnail_04.jpg");
		 HttpGet get = new HttpGet("https://d1natzk16yc4os.cloudfront.net/7c5d5197-9f15-4265-bd1b-524f2dc0a945/320x180.m3u8");
		 Main main = new Main();
//		String[] urls = main.createSignedURL(time, "https://d1natzk16yc4os.cloudfront.net/79bd3570-6f11-48ef-ad61-3c3ecc957752/thumbnails/79bd3570-6f11-48ef-ad61-3c3ecc957752_thumbnail_04.jpg"); 
//		HttpGet get2 = new HttpGet(urls[0]);
//
//		URL signedAsURL = new URL(urls[0]);
		
//		Map<String, String> params = getQueryMap(signedAsURL.getQuery());
		
//		String urlPolicy = params.get("Policy");
//		String cookiePolicy = generated.get("CloudFront-Policy");
//		if(cookiePolicy.equals(urlPolicy)){
//			System.out.println("policies match");
//		} else {
//			System.out.println("policies dont match");
//			System.out.println("URL policy: " + urlPolicy);
//			System.out.println("cookie pol: " + cookiePolicy);
//			System.out.println("");
//			System.out.println("url policy: " + urls[1]);
//			System.out.println("cook polic: " + generated.get("original"));
//		}
//		
//		
//		String urlSignature = params.get("Signature");
//		String cookieSignature = generated.get("CloudFront-Signature");
//		if(cookieSignature.equals(urlSignature)){
//			System.out.println("signatures match");
//		} else {
//			System.out.println("signatures dont match");
//			System.out.println("URL signat: " + urlSignature );
//			System.out.println("cookie sig: " + cookieSignature);
//		}
//		
//		String urlKeyPair = params.get("Key-Pair-Id");
//		String  cookieKeyPair    = generated.get( "CloudFront-Key-Pair-Id");
//		if(cookieKeyPair.equals(urlKeyPair )){
//			System.out.println("keypairs match");
//		} else {
//			System.out.println("keypairs dont match");
//			System.out.println("URL key   : " + urlKeyPair  );
//			System.out.println("cookie key: " + cookieKeyPair);
//		}
		
		CookieStore cookieStore = new BasicCookieStore(); 
		
		StringBuilder buf = new StringBuilder();
		for(String name : generated.keySet()){
			if(name.contains("original")) continue; 
			buf.append(name + "=" + generated.get(name) +"; ");
		}
		
		String result = buf.toString();
		result = result.substring(0, result.length() - 2);
		get.addHeader("Cookie", result);
//		
//		context.setCookieStore(cookieStore);
		 CloseableHttpClient httpClient = HttpClients.custom().build();

		CloseableHttpResponse resp = httpClient.execute(get);
	
		System.out.println("resp: " + resp.getStatusLine().getStatusCode());
	
		
		
	}
	
	public static Map<String, String> getQueryMap(String query)
	{
	    String[] params = query.split("&");
	    Map<String, String> map = new HashMap<String, String>();
	    for (String param : params)
	    {
	        String name = param.split("=")[0];
	        String value = param.split("=")[1];
	        map.put(name, value);
	    }
	    return map;
	}
	
//	public String[] createSignedURL(long expirationdate, String baseUrl) throws CloudFrontServiceException, ParseException, FileNotFoundException, IOException{
//		// Signed URLs for a private distribution
//		// Note that Java only supports SSL certificates in DER format, 
//		// so you will need to convert your PEM-formatted file to DER format. 
//		// To do this, you can use openssl:
//		// openssl pkcs8 -topk8 -nocrypt -in origin.pem -inform PEM -out new.der 
////		    -outform DER 
//		// So the encoder works correctly, you should also add the bouncy castle jar
//		// to your project and then add the provider.
//
//		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//
//		String distributionDomain = "a1b2c3d4e5f6g7.cloudfront.net";
//		String privateKeyFilePath = "private.der";
//		String policyResourcePath = baseUrl;
//		// Convert your DER file into a byte array.
//
//		byte[] derPrivateKey = ServiceUtils.readInputStreamToBytes(new
//		    FileInputStream(privateKeyFilePath));
//		URL icon = getClass().getResource("/private.der");
//		byte[] derPrivateKeyLocal = Resources.toByteArray(icon);
//
////		// Generate a "canned" signed URL to allow access to a 
////		// specific distribution and object
////
////		String signedUrlCanned = CloudFrontService.signUrlCanned(
////				policyResourcePath, // Resource URL or Path
////		    "APKAJ5JLXNMQ47T6GNGQ",     // Certificate identifier, 
////		                   // an active trusted signer for the distribution
////		    derPrivateKey, // DER Private key data
////		    ServiceUtils.parseIso8601Date("2015-12-20T22:20:00.000Z") // DateLessThan
////		    );
//		
//
//		// Build a policy document to define custom restrictions for a signed URL.
//		//Date date = ServiceUtils.parseIso8601Date("2015-12-20T00:00:00.000Z");
//                      
//		Date date = new Date(expirationdate);  
//		String[] results = new String[2];
//				
//		String policy = CloudFrontService.buildPolicyForSignedUrl(
//		    // Resource path (optional, can include '*' and '?' wildcards)
//		    policyResourcePath, 
//		    // DateLessThan
//		    date, 
//		    // CIDR IP address restriction (optional, 0.0.0.0/0 means everyone)
//		    "0.0.0.0/0", 
//		null
//		    );
//
//		results[1] = policy;
//		// Generate a signed URL using a custom policy document.
//
//		String signedUrl = CloudFrontService.signUrl(
//		    // Resource URL or Path
//		    policyResourcePath, 
//		    // Certificate identifier, an active trusted signer for the distribution
//		    "APKAJ5JLXNMQ47T6GNGQ",     
//		    // DER Private key data
//		    derPrivateKey, 
//		    // Access control policy
//		    policy 
//		    );
//		
//		results[0] = signedUrl; 
//		return results;
//	}

}
