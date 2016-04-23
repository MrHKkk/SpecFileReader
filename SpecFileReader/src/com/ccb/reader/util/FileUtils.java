package com.ccb.reader.util;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * 输出错误文件
 * @author KH
 *
 */
public class FileUtils {

	
	/**
	 * 输出扫描DS文件时解析错误的SQL
	 * @param outFilePath
	 * @param originalFileName
	 * @param keyWords
	 * @param sql
	 */
	public static void writeCognosErrorFile(String outFilePath, String originalFileName, List<String> keyWords, String sql, String thread) {
		BufferedWriter writer = null;
		try {
			File fileDir = new File(outFilePath);
			if(!fileDir.exists()) {
				fileDir.mkdir();
			}
			String packName = keyWords.get(0);
			String qryName = keyWords.get(1);
			String dataSource = keyWords.get(2);
			File errorFile = new File(outFilePath
					+ "_"
					+ packName
					+ "_"
					+ qryName
					+ "_"
					+ dataSource
					+ "_"
					+ originalFileName
							.replace(
									".xml",
									"")
					+ ".txt");
			if(!errorFile.exists()) {
				errorFile.createNewFile();
			}
			
			
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(errorFile)));
			writer.write("In file "
					+ originalFileName + "\n" + "QryName " + qryName
					+ "\n" + "DataSource" + dataSource + "\n"
					+ "Can not parse \n" + sql
					+ "\n" );
			writer.flush();
			
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
 	}
}
