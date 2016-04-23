package com.ccb.reader;

import java.io.File;



public interface SpecFileReader extends Runnable {

	public Object parseXmlFile(File file);
	
	public void constructPersistObj(File file);
	
	public void persistData();
}
