package com.nostalgia.aws;

public class AWSConfig {

	public String distributionDomain = "d1natzk16yc4os.cloudfront.net";
	public String privateKeyFilePath = "private.der";
	public String s3ObjectKey = "s3/object/key.txt";
	public String keyPairId = "APKAJ5JLXNMQ47T6GNGQ";
	
	
	public String getKeyPairId() {
		// TODO Auto-generated method stub
		return null;
	}



	public String getPrivateKeyFilePath() {
		return privateKeyFilePath;
	}



	public void setPrivateKeyFilePath(String privateKeyFilePath) {
		this.privateKeyFilePath = privateKeyFilePath;
	}

}
