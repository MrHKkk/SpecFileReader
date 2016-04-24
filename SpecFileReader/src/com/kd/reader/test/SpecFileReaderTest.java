package com.kd.reader.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kd.reader.CognosXmlReader;
import com.kd.reader.SpecFileReader;

public class SpecFileReaderTest {
	
	public static void main(String[] args) {
		File file = new File("D:\\java_code\\work\\cognos");
		List<File> list = new ArrayList<File>();
		if(file.isDirectory()) {
			File files[] = file.listFiles();
			for(int i = 50; i < 100; i++) {
				if(files[i].isFile()) {
					list.add(files[i]);
				}
				
			}
		} else {
			list.add(file);
		}
		SpecFileReader reader = new CognosXmlReader(list);
		Thread t1 = new Thread(reader);
		t1.start();
	}
}
