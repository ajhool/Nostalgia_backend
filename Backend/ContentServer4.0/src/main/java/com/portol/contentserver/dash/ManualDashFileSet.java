package com.portol.contentserver.dash;

import java.io.File;
import java.util.List;

import com.castlabs.dash.dashfragmenter.cmdlines.DashFileSet;

public class ManualDashFileSet extends DashFileSet{

	public static final boolean EXPLODE_DEFAULT = true;
	public static final boolean ENABLE_VERBOSE = true;
	
	//TODO constructor
	
	public ManualDashFileSet(File outputDir, List<File> inputFiles) {
		super();
		if(outputDir != null){
		this.outputDirectory = outputDir;
		}
		this.inputFiles = inputFiles;
		this.explode = EXPLODE_DEFAULT;
		this.verbose = ENABLE_VERBOSE;
	}
	
	public void setOutputDir(File output){
		super.outputDirectory = output;
	}
	
	public ManualDashFileSet() {
		super();
		this.explode = EXPLODE_DEFAULT;
		this.verbose = ENABLE_VERBOSE;
	}

	public void setInputFiles(List<File> inputFiles){
		super.inputFiles = inputFiles;
	}
	
	public void setExplode(boolean val){
		super.explode = val;
	}
	
	public void setVerbose(boolean enable){
		super.verbose = enable;
	}
}