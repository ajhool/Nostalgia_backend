package com.nostalgia;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.nostalgia.persistence.model.IdenticonRequest;

public class IdenticonGenFunction implements RequestHandler<String, String> {

	@Override
	public String handleRequest(String in, Context context) {
		return "Hello World"; 
	}

}
