package com.nostalgia.persistence.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessContentResponsePojo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4506928846733395722L;
	
	public List<String> generated_files; 

}
