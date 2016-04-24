package com.kd.reader;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.kd.reader.db.DbcpBean;

/**
 * 入口类
 * @author KH
 *
 */
public class SpecFileMainReader {
	
	private final static String DS = "DS";
	private final static String COGNOS = "COGNOS";
	
	private final static String SINGLE = "SINGLE";
	private final static String MULTI = "MULTI";
	
	
	
	/**
	 * 使用多线程解析文件时，首先在参数中指定线程数(2-4个线程，不推荐过多)
	 * 再指定所需解析文件的路径，单文件夹，如果含有子文件夹会默认不处理，
	 * 最后再给每个线程分配文件，按照文件个数决定(文件大小，复杂度对分配原则没有影响)。
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println("***************************************************************************");
			System.out.println("*没有足够的参数 至少需要前3个参数                                                                                       *");
			System.out.println("*参数1.确定解析DS还是Cognos                             				      *");
			System.out.println("*参数2.解析文件名                                       				                          *");
			System.out.println("*参数3.错误文件输出路径                                 				                      *");
			System.out.println("*参数4.解析类型 (-single单个文件 -multi文件夹)        					      *");
			System.out.println("*参数5.线程数                                       					                          *");
			System.out.println("*参数6.是否同一文件 1表示同一源文件 2表示非同一源文件   	         	          *");
			System.out.println("***************************************************************************");
			System.exit(0);
		}
		DbcpBean db = new DbcpBean("jdbc:oracle:thin:@localhost:1521:sys", "odsb", "odsb",
				"oracle.jdbc.driver.OracleDriver", 5, 5, 30000);
//		DbcpBean db1 = new DbcpBean("jdbc:oracle:thin:@56.0.98.2:1521:hzadb", "odsb", "odsb",
//				"oracle.jdbc.driver.OracleDriver", 5, 5, 30000);
		if(DS.equals(args[0].toUpperCase()) && (SINGLE.equals(args[3]) || args[3] == null )) {
			File file = new File(args[1]);
			if(!file.exists()) {
				System.out.println(args[1] + "文件不存在" );
				System.exit(0);
			}
			String outputFilePath = args[2];
			SpecFileReader reader = new DSXmlReader(file, outputFilePath, file.getName());
			//解析文件
			reader.parseXmlFile(file);
			//构建数据结构
			reader.constructPersistObj(file);
			//持久化数据
			reader.persistData();
		} else if(DS.equals(args[0].toUpperCase()) && MULTI.equals(args[3].toUpperCase())) {
			List<Thread> threadList = new ArrayList<Thread>();
			File file = new File(args[1]);
			if(!file.isDirectory()) {
				System.out.println("批量文件扫描需要参数为文件夹!");
				System.exit(0);
			}
			File[] files = file.listFiles();
			if(args[4] != null) { //多线程执行解析
				int total = files.length;
				int threadCount = Integer.parseInt(args[4]);
				int perThreadFiles = total / threadCount;
				int modValue = total % threadCount;
				for(int i = 0; i < threadCount; i++) {
					List<File> list = new ArrayList<File>();
					for(int j = 0; j < perThreadFiles; j++) {
						//如果是文件夹则不添加进行待处理文件列表
						if(files[j].isDirectory()) {
							continue;
						}
						if(i == 0){
							list.add(files[j]);
						} else {
							list.add(files[perThreadFiles * i + j ]);
						}
						
					}
					if(i == threadCount - 1) {
						while(modValue > 0) {
							list.add(files[total - modValue]);
							modValue--;
						}
						
					}
					SpecFileReader reader = new DSXmlReader(list, args[2], file.getName(), args[5]);
					Thread thread = new Thread(reader);
					threadList.add(thread);
				}
				for(Thread t: threadList) {
					t.start();
				}
			} else { //单线程执行解析
				List<File> parseList = new ArrayList<File>();
				
				for(File f : files) {
					parseList.add(f);
				}
				String outputFilePath = args[2];
				SpecFileReader reader = new DSXmlReader(parseList, outputFilePath,file.getName(), args[5] );
				Thread thread = new Thread(reader);
				thread.start();
			}
		}
		else if(COGNOS.equals(args[0].toUpperCase())) {
			List<Thread> threadList = new ArrayList<Thread>();
			File file = new File(args[1]);
			if(!file.isDirectory()) {
				System.out.println("批量文件扫描需要参数为文件夹!");
				System.exit(0);
			}
			String outputFilePath = args[2];
			File outputFile = new File(outputFilePath);
			if(outputFile.isFile()) {
				System.out.println("错误输出文件类型需要为文件夹");
			}
			File[] files = file.listFiles();
			//分配N个线程解析文件，根据文件个数判定每个线程要解析的个数
			if(args[4] != null) {
				int total = files.length;
				int threadCount = Integer.parseInt(args[4]);
				int perThreadFiles = total / threadCount;
				int modValue = total % threadCount;
				for(int i = 0; i < threadCount; i++) {
					List<File> list = new ArrayList<File>();
					for(int j = 0; j < perThreadFiles; j++) {
						if(files[j].isDirectory()) {
							continue;
						}
						if(i == 0){
							list.add(files[j]);
						} else {
							list.add(files[perThreadFiles * i + j ]);
						}
						
					}
					if(i == threadCount - 1) {
						while(modValue > 0) {
							list.add(files[total - modValue]);
							modValue--;
						}
						
					}
					SpecFileReader reader = new CognosXmlReader(list, outputFilePath);
					Thread thread = new Thread(reader);
					threadList.add(thread);
				}
				for(Thread t: threadList) {
					t.start();
				}
			}
		}
		
		try {
			db.shutdown();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		

	}
}
