package com.kd.reader;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;



public class BigFileCognosReader {

	private static String filePath = "C:\\dsxml\\DSExport183_1102.xml";
	
	private static String outFilePath = "C:\\split_file1\\";
	
	public static void main(String[] args) throws FileNotFoundException {
		BufferedReader reader = null;
		FileOutputStream fos = null;
		int fileNo = 1;
		boolean flag = false;
		try {

			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			String str = null;
			String header = null;
			long begin = System.currentTimeMillis();
			while((str = reader.readLine()) != null) {
				
				if(str.trim().toUpperCase().startsWith("<DSEXPORT>")) {
					flag = true;
					continue;
				}
				if(str.trim().toUpperCase().startsWith("<HEADER")) {
					if(header == null) {
						header = str;
					}
					continue;
				}
				if(str.trim().toUpperCase().startsWith("<JOB")) {
					
					sb.append("<DSExport>");
					sb.append(header);
					sb.append(str);
					continue;
				} else if(str.trim().toUpperCase().startsWith("</JOB>")) {
					sb.append(str);
					outFilePath += "outFile" + fileNo + ".xml";
					fos = new FileOutputStream(outFilePath);
					fileNo++;
					sb.append("</DSExport>");
					fos.write(sb.toString().getBytes());
					fos.flush();
					fos.close();
					System.out.println("Write a file done. ");
					sb = new StringBuilder();
					sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
					outFilePath = "C:\\split_file1\\";
					continue;
				} else {
					if(flag) {
						sb.append(str);
					}
					
				}
				
			}
			long end = System.currentTimeMillis();
			System.out.println("cost " + (end - begin) / 1000 + "sec");
		} catch (IOException e) {
			
			e.printStackTrace();
		} finally {
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				
					e.printStackTrace();
				}
			}
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
