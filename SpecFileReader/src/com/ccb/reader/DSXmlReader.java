package com.ccb.reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.ccb.reader.model.DSJobInfo;
import com.ccb.reader.model.DSWidgetInfo;
import com.ccb.reader.model.DSXmlHeader;
import com.ccb.reader.util.DBUtils;
import com.ccb.reader.util.DateUtils;

@SuppressWarnings("unchecked")
public class DSXmlReader implements SpecFileReader{

	
	public DSXmlReader() {
		
	}
	
	public DSXmlReader(List<File> file2Parse, String outputFilePath, String splitFile, String sameFile) {
		this.parseList = file2Parse;
		this.outputFilePath = outputFilePath;
		this.splitFile = splitFile;
		this.sameFile = sameFile;
	}
	
	public DSXmlReader(List<File> file2Parse, String outputFilePath, String splitFile) {
		this.parseList = file2Parse;
		this.outputFilePath = outputFilePath;
		this.splitFile = splitFile;
	}
	
	public DSXmlReader(File file2Parse, String outputFilePath, String splitFile) {
		this.file2Parse = file2Parse;
		this.outputFilePath = outputFilePath;
		this.splitFile = splitFile;
	}
	
	private static final Logger logger = Logger.getLogger(DSXmlReader.class); 
	
	private static final String toolInstanceId = "ToolInstanceID";
	private static final String serverName = "ServerName";
	private static final String serverVersion = "ServerVersion";
	
	private static final String oracle_specs_str_regexp_like = "regexp_like";
	
	private String splitFile; //从哪个文件解析而来
	
	private int totalError = 0;
	
	private String sameFile; //是否同一文件
	//单个转换文件
	private File file2Parse;
	//需要转换的文件列表
	private List<File> parseList;
	//错误文件输出路径
	private String outputFilePath;
	//存储RECORD (父子控件)
	private Map<DSWidgetInfo, List<DSWidgetInfo>> widgetMap = new HashMap<DSWidgetInfo, List<DSWidgetInfo>>();
	//临时存储子控件
	private List<DSWidgetInfo> widgetList = new ArrayList<DSWidgetInfo>();
	//存储DS JOB 
	private	List<DSJobInfo> jobInfoList = new ArrayList<DSJobInfo>();
	
	
	private static Map<String, String> jobIdMap = DBUtils.getAllJobId();
	
	private static int idGen = 0;
	
	private DSXmlHeader header = new DSXmlHeader();
	
	
	
	public void run() {
		int fileCount = 0;
		long begin = System.currentTimeMillis();
		for(File file : parseList) {
			if(!file.isDirectory()) {
				logger.info("Parse " + fileCount++ );
				//解析文件
				logger.info("Parsing file " + file.getName());
				parseXmlFile(file);
				
				//构建数据结构
				constructPersistObj(file);
				//持久化数据
				persistData();
				//清除本次内容
				widgetMap = new HashMap<DSWidgetInfo, List<DSWidgetInfo>>();
				widgetList = new ArrayList<DSWidgetInfo>();
				jobInfoList = new ArrayList<DSJobInfo>();
				logger.info("Per finished");
			}
		}
		long end = System.currentTimeMillis();
		long cost = end - begin;
		logger.info(Thread.currentThread().getName() + "Cost " + cost/1000 + "sec.");
		logger.info(Thread.currentThread().getName() + "Total file: " + fileCount);
	}
	
	/**
	 * 根据DS规则替换一些字符串
	 * @param sql
	 * @return
	 */
	private String dsSQLReplace(String sql) {
		sql = sql.replaceAll("\\(1\\)","");
		sql = sql.replaceAll("\\(2\\)","");
		sql = sql.replaceAll("\\(3\\)","");
		sql = sql.replaceAll("\\(20\\)", "");
		sql = sql.replaceAll("0\\\\","");
		sql = sql.replaceAll("\\\\0","");
		sql = sql.replaceAll("\\\\","");
		
		return sql;
	}
	
	
	public Object parseXmlFile(File file) {
		Map<DSWidgetInfo, List<DSWidgetInfo>> map = null;
		try {
			map = readDSXml(file);
		} catch(UnsupportedEncodingException ex) { 
			ex.printStackTrace();
		} catch(FileNotFoundException ex) {
			ex.printStackTrace();
		} catch(DocumentException ex) {
			ex.printStackTrace();
		}
		
		return map;
	}
	
	/**
	 * 读取DS文件并将数据封装到MAP中
	 * @param file
	 * @return
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 * @throws DocumentException 
	 */
	public  Map<DSWidgetInfo, List<DSWidgetInfo>> readDSXml(File file) throws UnsupportedEncodingException, FileNotFoundException, DocumentException {
		//File file = new File("c:\\test_truncate.xml");
		SAXReader reader = new SAXReader();
		
		//Map<DSWidgetInfo, List<DSWidgetInfo>> widgetMap = new HashMap<DSWidgetInfo, List<DSWidgetInfo>>();
		
		InputStreamReader inReader = new InputStreamReader(new FileInputStream(file), "utf-8");
		long begin = System.currentTimeMillis();
		Document doc = reader.read(inReader);
		long end = System.currentTimeMillis();
		
		
		
		logger.info("read file cost " + (end - begin) / 1000 + "sec");
		
		try {
			inReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info(doc);
		Element root = doc.getRootElement();
		logger.info(root);
		Element ele = root.element("Header");
		
		header.setDateModified(ele.attributeValue("Date"));
		header.setInstanceId(ele.attributeValue(toolInstanceId));
		header.setServerName(ele.attributeValue(serverName));
		header.setServerVersion(ele.attributeValue(serverVersion));
		header.setFromFile(this.splitFile);
		
		logger.info(ele.attribute(0));
		logger.info(ele.attribute("ServerName"));
		
		List<Element> elements = root.elements("Job");
		
		int times = 0;
		int fatherWidgetCount = 0;
		int sonWidgetCount = 0;
		for(Element jobEle : elements) {
			DSJobInfo job = new DSJobInfo();
			job.setDsInstId(header.getServerName());
			job.setJobIdentifier(jobEle.attributeValue("Identifier"));
			job.setDateModified(jobEle.attributeValue("DateModified"));
			String jobDesc = jobIdMap.get(job.getJobIdentifier());
			if(jobDesc != null && jobDesc.length() > 1) {
				String[] strs = jobDesc.split("_");
				job.setJobId(strs[0].trim());
				job.setRunStatus(strs[1].trim());
			}
			
			logger.info("------------------------------ new ds job --------------------------");
			logger.info(jobEle.attributeValue("Identifier"));
			logger.info(jobEle.attributeValue("DateModified"));
			List<Element> recordElements = jobEle.elements("Record");
			
			for(Element record : recordElements) {
				
				List<Element> collectionEle = record.elements("Collection");
				
				//获取JobInfo的信息
				//获取IdentList的内容
				for(Element collection : collectionEle) {
					if("MetaBag".equals(collection.attributeValue("Name"))) {
						List<Element> subRecordEle = collection.elements("SubRecord");
						for(Element subRecord : subRecordEle) {
							
							Iterator<Element> iter = subRecord.elementIterator();
							
							while(iter.hasNext()) {
								Element valueEle = iter.next();
								if("IdentList".equals(valueEle.getText()) && iter.hasNext()) {
									Element subPropEle = iter.next();
									job.setIdentListStr(subPropEle.getText());
									logger.info("identList " + subPropEle.getText());
									break;
								}
							}
						}
					}
				}
				
				//获取控件代码列表 ContainerId 
				if("ContainerView".equals(record.attributeValue("Type"))) {
					logger.info("container id " + record.attributeValue("Identifier"));
					List<Element> propsEle = record.elements("Property");
					for(Element prop : propsEle) {
						if("StageList".equals(prop.attributeValue("Name"))) {
							job.setStageListStr(prop.getText());
							logger.info("StageList " + prop.getText());
						}
					}
				}
				
				//获取JOB Category
				if("JobDefn".equals(record.attributeValue("Type"))) {
					List<Element> propsEle = record.elements("Property");
					for(Element prop : propsEle) {
						if("Category".equals(prop.attributeValue("Name"))) {
							logger.info("############################### " + prop.getText());
							times++;
							job.setJobCategory(prop.getText());
							logger.info("count " + times);
						}
					}
				}
				
				//获取所有父控件信息
				if("CustomStage".equals(record.attributeValue("Type"))  || "TransformerStage".equals(record.attributeValue("Type"))) {
					//获取父控件
					if("CustomStage".equals(record.attributeValue("Type")) || 
							"TransformerStage".equals(record.attributeValue("Type"))) {
						logger.info("father widget count: "  + fatherWidgetCount++);
						DSWidgetInfo widgetInfo = new DSWidgetInfo();
						widgetInfo.setStageId(idGen++);
						
						
						logger.info("job widget identifier: " + record.attributeValue("Identifier"));
						logger.info("job widget dateModified: " + record.attributeValue("DateModified"));
						if(job.getJobId() != null && !"".equals(job.getJobId())){
							widgetInfo.setInstId(job.getJobId());
						} else {
							widgetInfo.setInstId("0");
						}
						
						widgetInfo.setStageIdentifier(record.attributeValue("Identifier"));
						widgetInfo.setDateModified(record.attributeValue("DateModified"));
						widgetInfo.setParent(true);
						widgetInfo.setParentId("0");
						List<Element> propsEle = record.elements("Property");
						for(Element prop : propsEle) {
							if("Name".equals(prop.attributeValue("Name"))) {
								logger.info("job widget name: " + prop.getText());
								widgetInfo.setWidgetName(prop.getText());
							}
							if("StageType".equals(prop.attributeValue("Name"))) {
								logger.info("job stage type: " + prop.getText());
								widgetInfo.setStageType(prop.getText());							
							}
							
							if("OutputPins".equals(prop.attributeValue("Name"))) {
								logger.info("output pins: " + prop.getText());
								widgetInfo.setOutputPinsStr(prop.getText());
							}
							if("InputPins".equals(prop.attributeValue("Name"))) {
								logger.info("input pins: " + prop.getText());
								widgetInfo.setInputPinsStr(prop.getText());							
							}
						}
						widgetMap.put(widgetInfo, new ArrayList<DSWidgetInfo>());					
					}
				}
				
			}
			jobInfoList.add(job);
			//获取所有的子控件内容 并将子控件与父控件进行关联
			for(Element record : recordElements) {
				if("CustomInput".equals(record.attributeValue("Type"))  
						|| "CustomOutput".equals(record.attributeValue("Type"))) {
					if("V36S0P1".equals(record.attributeValue("Identifier"))) {
						logger.info("V36S0P1");
					}
					sonWidgetCount++;
					DSWidgetInfo widgetInfo = new DSWidgetInfo();
					if(job.getJobId() != null && !"".equals(job.getJobId())){
						widgetInfo.setInstId(job.getJobId());
					} else {
						widgetInfo.setInstId("0");
					}
					
					
					widgetInfo.setStageIdentifier(record.attributeValue("Identifier"));
					widgetInfo.setDateModified(record.attributeValue("DateModified"));
					widgetInfo.setStageType(record.attributeValue("Type"));
					widgetInfo.setParent(false);
					List<Element> propsEle = record.elements("Property");
					for(Element prop : propsEle) {
						if("Name".equals(prop.attributeValue("Name"))) {
							logger.info("job widget name: " + prop.getText());
							widgetInfo.setWidgetName(prop.getText());
						}
						if("Partner".equals(prop.attributeValue("Name"))) {
							logger.info("job widget name: " + prop.getText());
							widgetInfo.setPratnerId(prop.getText());
						}
					}
					//获取SQL,database server等信息
					List<Element> collectionEle = record.elements("Collection");
					for(Element collection : collectionEle) {
						if("CustomProperty".equals(collection.attributeValue("Type"))) {
							Iterator<Element> subIter = collection.elementIterator("SubRecord");
							while(subIter.hasNext()) {
								Element subRecordEle = subIter.next();
								//List<Element> propsEle1 = subRecordEle.elements("Property");
								Iterator<Element> propsIter = subRecordEle.elementIterator("Property");
								while(propsIter.hasNext()) {
									Element prop = propsIter.next();
									if(("query".equals(prop.getText()) || "update".equals(prop.getText())
											|| "insert".equals(prop.getText())) && propsIter.hasNext()) {
										Element prop2 = propsIter.next();
										logger.info("sql: " + prop2.getText());
										if(widgetInfo.getSqlStr() == null || "".equals(widgetInfo.getSqlStr())) {
											String sql = prop2.getText();
											sql = sql.replaceAll("insert\\\\", "");
											sql = sql.replaceAll("select\\\\", "");
											sql = sql.replaceAll("update\\\\", "");
											sql = sql.replaceAll("delete\\\\", "");
											sql = dsSQLReplace(sql);
											logger.info("original sql: " + sql);
											widgetInfo.setSqlStr(sql);
										} else {
											String sql = prop2.getText();
											sql = sql.replaceAll("insert\\\\", "");
											sql = sql.replaceAll("select\\\\", "");
											sql = sql.replaceAll("update\\\\", "");
											sql = sql.replaceAll("delete\\\\", "");
											sql = dsSQLReplace(sql);
											
											logger.info("original sql: " + sql);
											widgetInfo.setSqlStr(widgetInfo.getSqlStr() + "|" + sql);
										}
										
									}
									if("server".equals(prop.getText()) && propsIter.hasNext()) {
										Element prop2 = propsIter.next();
										logger.info("server: " + prop2.getText());
										widgetInfo.setDatabaseName(prop2.getText());
									}
									if("dboptions".equals(prop.getText()) && propsIter.hasNext()) {
										Element prop2 = propsIter.next();
										logger.info("user: " + prop2.getText());
										widgetInfo.setDatabaseUser(prop2.getText());
									}
									if("tablename".equals(prop.getText()) && propsIter.hasNext()) {
										Element prop2 = propsIter.next();
										String tableName = prop2.getText();
										tableName = tableName.replaceAll("0\\\\","");
										tableName = tableName.replaceAll("\\\\0","");
										tableName = tableName.replaceAll("\\\\","");
										tableName = tableName.replaceAll("\\(1\\)","");
										tableName = tableName.replaceAll("\\(2\\)","");
										tableName = tableName.replaceAll("\\(3\\)","");
										
										tableName = tableName.replaceAll("tablename", "");
										tableName = tableName.replaceAll("table", "");
										logger.info("user: " + prop2.getText());
										if(tableName != null && !"".equals(tableName)) {
											widgetInfo.setTableName(tableName);
										}
									}
								}
							
							}
						}
						
					}
					widgetList.add(widgetInfo);
					logger.info(widgetInfo);
				}
				
			}
		}
		return widgetMap;
	}
	
	/**
	 * 获取tableName
	 * @param sql
	 * @return
	 * @throws JSQLParserException
	 * @throws UnsupportedEncodingException
	 */
	public  List<String> parseSqlTable(String sql) throws JSQLParserException, UnsupportedEncodingException {
		logger.info("parsing " + sql);
		sql = sql.toUpperCase();
		sql = dsSQLReplace(sql);
		
		sql = sql.replaceAll("#", "");
		sql = sql.replaceAll("@", "_at_");
		sql = sql.replaceAll("INSERTARRAYSIZE[\\d][\\d][\\d]", "");
		sql = sql.replaceAll("INSERTARRAYSIZE[\\d][\\d]", "");
		sql = sql.replaceAll("INSERTARRAYSIZE[\\d]", "");
		sql = sql.replaceAll("\ufffd", "");
		sql = sql.replaceAll("\u3000", "");
		
		// 去除注释
		while (sql.contains("/*")) {
			int bIndex = sql.indexOf("/*");
			int eIndex = sql.indexOf("*/");

			String slashStr = sql.substring(bIndex, eIndex + 2);
			sql = sql.replace(slashStr, "");
		}
		while (sql.contains("--")) {
			int bIndex = sql.indexOf("--");
			String subStr = sql.substring(bIndex + 1, sql.length());
			int eIndex = subStr.indexOf("\n");
			String slashStr = null;
			if(eIndex != -1) {
				slashStr = sql.substring(bIndex, eIndex + bIndex + 1);
			} else {
				slashStr = sql.substring(bIndex, sql.length());
			}
			
			sql = sql.replace(slashStr, "");
		}
		// 替换中文文字
		for (int i = 0; i < sql.length(); i++) {
			char ch = sql.charAt(i);
			int v = (int) ch;
			if (v >= 19968 && v <= 171941) {
				sql = sql.replace(ch, ' ');
			}
		}
 		sql = sql.replaceAll("< =", "<=");
		sql = sql.replaceAll("> =", ">=");
		sql = sql.replaceAll("< >", "<>");
		sql = sql.replaceAll("! =", "!=");
		
		sql = sql.replaceAll("LIMIT", "li");
		if(!sql.toUpperCase().trim().startsWith("SELECT") && !sql.toUpperCase().trim().startsWith("DELETE") && 
				!sql.toUpperCase().trim().startsWith("INSERT") && !sql.toUpperCase().trim().startsWith("UPDATE") ) {
			logger.info("did not parse " + sql);
			return null;
		}
		Statement statement = CCJSqlParserUtil.parse(new String(sql
				.getBytes("UTF-8")));
		List<String> tableList = null;
		TablesNamesFinder finder = new TablesNamesFinder();
		
		if(sql.toUpperCase().trim().startsWith("SELECT")) {
			Select selectStatement = (Select) statement;
			tableList  = finder.getTableList(selectStatement);
		} else if(sql.toUpperCase().trim().startsWith("INSERT")) {
			Insert insertStatement = (Insert) statement;
			tableList  = finder.getTableList(insertStatement);
		} else if(sql.toUpperCase().trim().startsWith("DELETE")) {
			Delete deleteStatement = (Delete)statement;
			tableList = finder.getTableList(deleteStatement);
		} else if(sql.toUpperCase().trim().startsWith("UPDATE")) {
			Update updateStatement = (Update)statement;
			tableList = finder.getTableList(updateStatement);
		}

		 
		return tableList;
	}
	
	public void constructPersistObj(File file) {
		Map<DSWidgetInfo, List<DSWidgetInfo>> map = mappingResource(this.widgetMap, this.widgetList);
		countParseWidget(map, file);
	}
	
	public void persistData() {
		logger.info("Persisting data....");
		DSObjPersistence.persistData(this.header, this.widgetMap, this.jobInfoList, sameFile);
		Date date = new Date(System.currentTimeMillis());
		
		logger.info("Finished at " + DateUtils.formatDate("yyyy-MM-dd HH:mm:ss", date));
	}
	
	/**
	 * 测试用函数
	 * @param args
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void main(String[] args) throws DocumentException, IOException {
		
		DSXmlReader reader = new DSXmlReader();
		
		File file = new File("C:\\split_file1\\outFile1.xml");
		logger.info("....reading file to memory ");
		reader.readDSXml(file);
		//jobEle 循环结束
		logger.info(reader.widgetMap.size());
		Set<DSWidgetInfo> set = reader.widgetMap.keySet();
		logger.info(reader.widgetList.size());
		logger.info(set.size());
		Map<DSWidgetInfo, List<DSWidgetInfo>> map = mappingResource(reader.widgetMap, reader.widgetList);
		Set<DSWidgetInfo> keySet = map.keySet();
		logger.info("keySet count: " + keySet.size());
		
		int count = 0;
		
		count = reader.countParseWidget(map, file);
		logger.info("parse sql count: " + count);
		logger.info(reader.jobInfoList);
		logger.info("persisting data....");
		DSObjPersistence.persistData(reader.header, reader.widgetMap, reader.jobInfoList, reader.sameFile);
		logger.info("DONE");
	}
	
	/**
	 * 解析每一个控件中的SQL
	 * @param map
	 * @param file
	 * @return
	 */
	public int countParseWidget(Map<DSWidgetInfo, List<DSWidgetInfo>> map, File file) {
		Set<DSWidgetInfo> keySet = map.keySet();
		Iterator<DSWidgetInfo> iter = keySet.iterator();
		int subCount = 0;
		StringBuilder sb = new StringBuilder();
		while(iter.hasNext()) {
			DSWidgetInfo widgetInfo = iter.next();
			if(widgetInfo.getSqlStr() != null && !"".equals(widgetInfo.getSqlStr())) {
				String tableName = null;
				try {
					List<String> list = parseSqlTable(widgetInfo.getSqlStr());
					if(list != null) {
						for(int i = 0; i < list.size(); i++) {
							if(i == list.size() -1) {
								tableName += list.get(i);
							} else {
								tableName += list.get(i) + "|";
							}
						}
					}
//					for(String str : list) {
//						tableName += str + "|";
//						
//					}
				} catch(JSQLParserException ex) {
					logger.error(widgetInfo.getSqlStr());
				} catch(UnsupportedEncodingException ex) {
					ex.printStackTrace();
				}
				
				widgetInfo.setTableName(tableName);
			}
			List<DSWidgetInfo> list = map.get(widgetInfo);
			for(DSWidgetInfo subWidgetInfo : list) {
				//logger.info(subWidgetInfo.getStageIdentifier());
				sb.append(subWidgetInfo.getStageIdentifier() + "|");
				if(subWidgetInfo.getSqlStr() != null && !"".equals(subWidgetInfo.getSqlStr())) {
					String tableName = "";
					try {
						//含有多个SQL 部分SQL会含有正则表达式使用'|' 需要处理
						if(subWidgetInfo.getSqlStr().contains("|") && subWidgetInfo.getSqlStr().indexOf("||") == -1
								&& subWidgetInfo.getSqlStr().indexOf(oracle_specs_str_regexp_like) == -1) {
							String[] sqls = subWidgetInfo.getSqlStr().split("\\|");
							
							for(String sqlStr : sqls) {
								List<String> subList = parseSqlTable(sqlStr);
								if(subList != null) {
									for(int i = 0; i < subList.size(); i++) {
										if(i == subList.size() -1) {
											tableName += subList.get(i);
										} else {
											tableName += subList.get(i) + "|";
										}
									}
								}
							}
						} else {
							
							List<String> subList = parseSqlTable(subWidgetInfo.getSqlStr());
							if(subList != null) {
								for(int i = 0; i < subList.size(); i++) {
									if(i == subList.size() -1) {
										tableName += subList.get(i);
									} else {
										tableName += subList.get(i) + "|";
									}
								}
							}
						}
					}  catch(JSQLParserException ex) {
						ex.printStackTrace();
						logger.error("error parsing file " +  file.getName() + ":" + "error parsing SQL: " + subWidgetInfo.getSqlStr());
						File fileDir = new File(outputFilePath);
						if(!fileDir.exists()) {
							fileDir.mkdir();
						}
						File errorFile = new File(outputFilePath + "ds_parse_error_" + file.getName() + "_" + Thread.currentThread().getName() +  ".txt");
						BufferedWriter errorWriter = null;
						FileOutputStream fos = null;
						OutputStreamWriter os = null;
						try {
							fos = new FileOutputStream(errorFile);
							os = new OutputStreamWriter(fos);
							errorWriter = new BufferedWriter(os);

							errorWriter.write("Can not parse StageIdentifier: " + subWidgetInfo.getStageIdentifier() + "\n" 
									+ " stageName " + subWidgetInfo.getWidgetName() + "\n"
									+ " sqlQry " + subWidgetInfo.getSqlStr() + "\n"
									+ " stageType " + subWidgetInfo.getStageType() + "\n"
									+ "");
							errorWriter.flush();
							errorWriter.close();
							os.close();
							fos.close();
							
						} catch (IOException ex1) {
							ex1.printStackTrace();
						} finally {
							if(os != null) {
								try {
									os.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							if(fos != null) {
								try {
									fos.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					} catch(UnsupportedEncodingException ex) {
						ex.printStackTrace();
					}
					subWidgetInfo.setTableName(tableName);
				}
				subCount++;
				
			}
		}
		logger.info("sub widget count: " + subCount);
		return subCount;
	}
	
	
	
	/**
	 * 将父子关系的控件相关联
	 * @param map
	 * @param list
	 * @return
	 */
	public static Map<DSWidgetInfo, List<DSWidgetInfo>> mappingResource(Map<DSWidgetInfo, List<DSWidgetInfo>> map, 
			List<DSWidgetInfo> list) {
		Set<DSWidgetInfo> keySet = map.keySet();
		Iterator<DSWidgetInfo> iter = keySet.iterator();
		int addTimes = 0;
		while(iter.hasNext()) {
			List<String> inputPinsList = null;
			List<String> outputPinsList = null;
			DSWidgetInfo fatherWidget = iter.next();
			if(fatherWidget.getInputPinsStr() != null) {
				 inputPinsList = splitPins(fatherWidget.getInputPinsStr());
			}
			if(fatherWidget.getOutputPinsStr() != null) {
				 outputPinsList = splitPins(fatherWidget.getOutputPinsStr());
			}
			
			Iterator<DSWidgetInfo> subIter = list.iterator();
			while(subIter.hasNext()) {
				DSWidgetInfo sonWidget = subIter.next();
				
				if(inputPinsList != null && !inputPinsList.isEmpty()) {
					for(String inputStr : inputPinsList) {
						if(inputStr.equals(sonWidget.getStageIdentifier())) {
							map.get(fatherWidget).add(sonWidget);
							addTimes++;
							subIter.remove();					
							logger.info("添加次数: " + addTimes);
							logger.info("剩余对象: " + list.size());
							
						}
					}
				}
				if(outputPinsList != null && !outputPinsList.isEmpty()) {
					for(String outputStr : outputPinsList) {
						if(outputStr.equals(sonWidget.getStageIdentifier())) {
							map.get(fatherWidget).add(sonWidget);
							addTimes++;
							subIter.remove();					
							logger.info("添加次数: " + addTimes);
							logger.info("剩余对象: " + list.size());
							
						}
					}
				}
			}
	
		}
		
		return map;
	}

	
	/**
	 * 分割OutputPins 或者 InputPins(可能有多个用‘|’进行组合)
	 * @param str
	 * @return
	 */
	public static List<String> splitPins(String str) {
		List<String> list = new ArrayList();
		if(str.contains("|")) {
			String[] splitStr = str.split("\\|");
			for(String newStr : splitStr) {
				list.add(newStr);
			}
		} else {
			list.add(str);
		}
		
		return list;
	}

	public List<File> getParseList() {
		return parseList;
	}

	public void setParseList(List<File> parseList) {
		this.parseList = parseList;
	}

	public String getOutputFilePath() {
		return outputFilePath;
	}

	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	public File getFile2Parse() {
		return file2Parse;
	}

	public void setFile2Parse(File file2Parse) {
		this.file2Parse = file2Parse;
	}

	public String getSplitFile() {
		return splitFile;
	}

	public void setSplitFile(String splitFile) {
		this.splitFile = splitFile;
	}

	public String getSameFile() {
		return sameFile;
	}

	public void setSameFile(String sameFile) {
		this.sameFile = sameFile;
	}
	
	
}
