package com.portol.contentserver;

public class FileMetadata {

	private final String name;
	private final Object value;
	
	
	public FileMetadata(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

}
