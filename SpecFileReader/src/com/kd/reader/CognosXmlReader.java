package com.kd.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import java.io.UnsupportedEncodingException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.kd.reader.db.DbcpBean;
import com.kd.reader.model.CognosXmlObj;
import com.kd.reader.model.SqlQueryObj;
import com.kd.reader.util.DBUtils;
import com.kd.reader.util.DateUtils;
import com.kd.reader.util.FileUtils;

@SuppressWarnings("unchecked")
public class CognosXmlReader implements SpecFileReader {

	public CognosXmlReader(List<File> file2Parse) {
		this.file2Parse = file2Parse;
	}
	
	public CognosXmlReader(List<File> file2Parse, String outputFilePath) {
		this.file2Parse = file2Parse;
		this.outputFilePath = outputFilePath;
	}

	public CognosXmlReader(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}
	
	public CognosXmlReader() {
	}
	
	
	private String outputFilePath; //错误文件输出路径

	private List<File> file2Parse;

	private static final Logger logger = Logger
			.getLogger(CognosXmlReader.class);

	private static final String class_kw = "//class";

	private static final String report = "report";

	private static final String metadataModel = "metadataModel";
	private static final String specification = "specification";
	private static final String query = "query";

	private static final String xmlBegin = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private int fileCount = 0;
	private int sqlCount = 0;
	private int errorSqlCount = 0;
	
	List<CognosXmlObj> persistList = new ArrayList<CognosXmlObj>();

	/**
	 * 根据传入的文件集合逐个处理文件
	 */
	public void run() {
		long begin = System.currentTimeMillis();
		logger.info("thread id:" + Thread.currentThread().getId() + " name:"
				+ Thread.currentThread().getName() + " started");
		List<CognosXmlObj> mergeList = new ArrayList<CognosXmlObj>();
		for (File file : file2Parse) {
			logger.info("parsing file " + file.getName());
			List<CognosXmlObj> list = parseFile(file);
			
			logger.info("parsing file " + file.getName() + " OK...");
			for (CognosXmlObj obj : list) {
				mergeList.add(obj);
			}
			
		}
		try {
			persistCognosXmlObj(mergeList);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		logger.info("thread id:" + Thread.currentThread().getId() + " name:"
				+ Thread.currentThread().getName() + " stoped");
		long end = System.currentTimeMillis();
		logger.info("time cost " + (end - begin) / 1000 + " sec");
		logger.info("parse sql count:" + sqlCount);
		logger.info("total parse file:" + fileCount);
		logger.info("error sql count:" + errorSqlCount);
	}

	/**
	 * 获取所有的REPORT
	 * 
	 * @param conn
	 * @return
	 * @throws IOException
	 */
	public List<CognosXmlObj> findAllCognosXmlObj(Connection conn)
			throws IOException {
		StringBuilder sqlQry = new StringBuilder();
		sqlQry
				.append("SELECT id, rptname, SQLStr, utabname, rpttype, datasource, qryname FROM RptSourceTabInfoList");
		List<CognosXmlObj> list = new ArrayList<CognosXmlObj>();
		java.sql.Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQry.toString());
			while (rs.next()) {
				boolean flag = true;
				if (list.size() != 0) {
					for (CognosXmlObj innerObj : list) {
						if (innerObj.getId().equals(rs.getString("id"))
								&& innerObj.getReportName().equals(
										rs.getString("rptname"))) {
							SqlQueryObj sqlObj1 = new SqlQueryObj();

							sqlObj1.setDataSource(rs.getString("datasource"));
							sqlObj1.setQueryName(rs.getString("qryname"));
							sqlObj1.setFromTable(rs.getString("utabname"));
							Reader reader1 = rs.getClob("SQLStr")
									.getCharacterStream();
							BufferedReader bufReader1 = new BufferedReader(
									reader1);
							StringBuilder sqlQryStr1 = new StringBuilder();
							String str1 = null;
							while ((str1 = bufReader1.readLine()) != null) {
								sqlQryStr1.append(str1);
							}
							sqlObj1.setQuerySql(sqlQryStr1.toString());
							innerObj.getQueryObjs().add(sqlObj1);
							flag = false;
						}
					}

				}
				if (flag) {
					CognosXmlObj obj = new CognosXmlObj();
					obj.setId(rs.getString("id"));
					obj.setReportName(rs.getString("rptname"));
					List<SqlQueryObj> sqlList = new ArrayList<SqlQueryObj>();
					obj.setQueryObjs(sqlList);
					SqlQueryObj sqlObj = new SqlQueryObj();

					sqlObj.setDataSource(rs.getString("datasource"));
					sqlObj.setQueryName(rs.getString("qryname"));
					sqlObj.setFromTable(rs.getString("utabname"));
					Reader reader = rs.getClob("SQLStr").getCharacterStream();
					BufferedReader bufReader = new BufferedReader(reader);
					StringBuilder sqlQryStr = new StringBuilder();
					String str = null;
					while ((str = bufReader.readLine()) != null) {
						sqlQryStr.append(str);
					}
					sqlObj.setQuerySql(sqlQryStr.toString());
					obj.getQueryObjs().add(sqlObj);
					list.add(obj);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return list;
	}

	/**
	 *  
	 * @param list
	 */
	private void persistCognosXmlObj(List<CognosXmlObj> list)
			throws IOException {

		Connection conn = DbcpBean.getConn();
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		StringBuilder sqlQry = new StringBuilder();
		sqlQry
				.append(
						"INSERT INTO RptSourceTabInfoList (id, rptname, rptpath, utabname, rpttype, uptime, datasource, SQLStr, qryname, filename) ")
				.append(" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		StringBuilder sqlQry1 = new StringBuilder();
		sqlQry1
				.append(
						"UPDATE RptSourceTabInfoList SET SQLStr = ?, datasource = ?, uptime = ?, utabname = ?, rptpath = ? ")
				.append("WHERE id = ? and rptname = ? and SQLStr = ?");

		try {
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sqlQry.toString());
			pstmt1 = conn.prepareStatement(sqlQry1.toString());
			for (CognosXmlObj cognosObj : list) {
				if (cognosObj.getQueryObjs() == null) {
					continue;
				} else {
					List<SqlQueryObj> sqlList = cognosObj.getQueryObjs();
					for (SqlQueryObj sqlObj : sqlList) {
						if ((sqlObj.getDataSource() != null && sqlObj.getQueryName() != null
							&& !"".equals(sqlObj.getDataSource()) && !"".equals(sqlObj.getQueryName()))
								) {
							pstmt.setString(1, cognosObj.getId());
							pstmt.setString(2, cognosObj.getReportName());
							pstmt.setString(3, cognosObj.getPath());
							String tableStr = sqlObj.getFromTable();

							pstmt.setString(4, tableStr);
							pstmt.setString(5, "Cognos");
							Date date = new Date(System.currentTimeMillis());
							String dateStr = DateUtils.formatDate(
									"yyyy-MM-dd HH:mm:ss", date);
							pstmt.setString(6, dateStr);
							pstmt.setString(7, sqlObj.getDataSource());
							pstmt.setString(8, sqlObj.getQuerySql());
							pstmt.setString(9, sqlObj.getQueryName());
							pstmt.setString(10, cognosObj.getName());
							pstmt.addBatch();
						}

					}
				}
			}

			int[] insert = pstmt.executeBatch();
			pstmt.clearBatch();
			int[] update = pstmt1.executeBatch();
			pstmt1.clearBatch();
			conn.commit();

			conn.setAutoCommit(true);
			logger.info(" insert: " + insert + " udpate:" + update);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtils.closeSt(pstmt);
			DBUtils.closeSt(pstmt1);
			DBUtils.closeConn(conn);
		}

	}
	
	public void constructPersistObj(File file) {
		
	}
	
	public Object parseXmlFile(File file) {
		return parseFile(file);
	}

	public void persistData() {
		try {
			persistCognosXmlObj(persistList);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 逐个文件解析 一个文件有可能有多个report
	 * 
	 * @param file
	 */
	public List<CognosXmlObj> parseFile(File file) {
		if (file.isDirectory()) {
			logger.error("Can not parse directory");

		}
		fileCount++;
		BufferedWriter errorWriter = null;

		InputStreamReader inReader = null;
		InputStream in = null;
		List<CognosXmlObj> objList = new ArrayList<CognosXmlObj>();
		try {

			SAXReader reader = new SAXReader();

			// 指定文件编码 与要解析的XML文件编码匹配
			inReader = new InputStreamReader(new FileInputStream(file), "utf-8");

			Document doc = reader.read(inReader);
			Element root = doc.getRootElement();

			// 获取XML <class> 标签
			List<Element> snodes = root.selectNodes(class_kw);
			//

			for (Element ele : snodes) {
				//logger.info(ele.getText());

				// 判断 <class> 标签的值是否为report
				if (report.equals(ele.getText())) {
					Element objsEle = ele.getParent();
					// 每一个Report对应一个CognosXmlObj
					CognosXmlObj cxObj = new CognosXmlObj();
					cxObj.setName(file.getName());
					List<SqlQueryObj> sqlQryList = new ArrayList<SqlQueryObj>();
					cxObj.setQueryObjs(sqlQryList);
					List sList = objsEle.elements();
					// logger.info("objects contains objects : " +
					// sList.size());
					Node idNode = (Node) sList.get(3);

					// logger.info("id " + idNode.getText() + " "
					// + idNode.getName());
					cxObj.setId(idNode.getText());

					Node nameNode = (Node) sList.get(1);
					// logger.info("nameNode " + nameNode.getText());
					cxObj.setReportName(nameNode.getText());

					Element propsEle = (Element) sList.get(4);
					List<Element> propsList = propsEle.elements();
					// logger.info("propsList size : " + propsList.size());

					// 获取路径,dataSource等内容
					for (Element propsSubEle : propsList) {

						// 判断是否为<metadataModel>标签
						if (metadataModel.equals(propsSubEle.getName())) {
							Node pathNode = propsSubEle.element("value")
									.element("item").element("searchPath")
									.element("value");
							//logger.info(pathNode.getText());
							cxObj.setPath(pathNode.getText());
						}
						if (specification.equals(propsSubEle.getName())) {
							Node node = propsSubEle.element("value");
							// 截取<queries>标签段的内容并构造新的InputStream
							String str = node.getText();
							
							int strBegin = str.indexOf("<queries>");
							int strEnd = str.indexOf("</queries>");
							//有可能不包含<queries></queries>
							if(strBegin != -1 && strEnd != -1) {
								StringBuilder sb = new StringBuilder(xmlBegin);
								try {
									sb.append(new String(str.substring(strBegin, strEnd
											+ "</queries>".length())));
								} catch(Exception ex) {
									ex.printStackTrace();
									System.out.println(str);
								}
								SAXReader saxReader = new SAXReader();
								in = new ByteArrayInputStream(sb.toString()
										.getBytes("UTF-8"));
								Document valDoc = saxReader.read(in);
//								logger.info(valDoc.getRootElement());
								Element valRootEle = valDoc.getRootElement();
								List<Element> queryEle = valRootEle
										.selectNodes(query);
								int addTimes = 0;
								for (Element subQueryEle : queryEle) {
									// 获取sqlQuery
									if (subQueryEle.element("source").element(
											"sqlQuery") != null) {
										SqlQueryObj obj = new SqlQueryObj();
//										logger.info("sqlQuery attr: "
//												+ subQueryEle.element("source")
//														.element("sqlQuery")
//														.attribute(0));
//										logger.info("sqlQuery attr: "
//												+ subQueryEle.element("source")
//														.element("sqlQuery")
//														.attribute(1));
										

										obj.setQueryName(subQueryEle.element(
												"source").element("sqlQuery")
												.attribute(0).getValue());
										obj.setDataSource(subQueryEle.element(
												"source").element("sqlQuery")
												.attribute(1).getValue());

										String sql = subQueryEle.element("source")
												.element("sqlQuery").element(
														"sqlText").getText();
										obj.setQuerySql(sql);
										String s1 = obj.getQueryName();
										String s2 = obj.getDataSource();
										try {
											// 解析SqlQuery
											List<String> list = parseSql4Table(sql,
													file, s1, nameNode.getText(),
													s2);
											String tableStr = "";
											if (list != null && list.size() != 0) {
												for (int i = 0; i < list.size(); i++) {
													tableStr += list.get(i)
															.toString();
													if (i != list.size() - 1) {
														tableStr += "|";
													}
												}
												obj.setFromTable(tableStr);
											}
										} catch (Exception ex) {
											ex.printStackTrace();
											errorSqlCount++;
											
											logger.error("error parsing sql " + sql
													+ " in " + file);
											List<String> keyWords = new ArrayList<String>();
											keyWords.add(nameNode.getText());
											keyWords.add(s1);
											keyWords.add(s2);
											FileUtils.writeCognosErrorFile(
													outputFilePath, file.getName(), keyWords, 
														sql, Thread.currentThread().getName());
//											File errorFilePath = new File(outputFilePath);
//											if(!errorFilePath.exists()) {
//												errorFilePath.mkdir();
//											}
//											File errorFile = new File(
//													outputFilePath
//															+ "_"
//															+ nameNode.getText()
//															+ "_"
//															+ s1
//															+ "_"
//															+ s2
//															+ "_"
//															+ file.getName()
//																	.replace(
//																			".xml",
//																			"")
//															+ ".txt");
//											if (!errorFile.exists()) {
//												try {
//													errorFile.createNewFile();
//												} catch (IOException e) {
//
//													e.printStackTrace();
//												}
//											}
//											FileOutputStream fos = null;
//											OutputStreamWriter os = null;
//											try {
//												fos = new FileOutputStream(
//														errorFile);
//												os = new OutputStreamWriter(fos);
//												errorWriter = new BufferedWriter(os);
//
//												errorWriter.write("In file "
//														+ file.getName() + "\n" + "QryName " + s1
//														+ "\n" + "DataSource" + s2 + "\n"
//														+ "Can not parse \n" + sql
//														+ "\n" );
//												errorWriter.flush();
//												errorWriter.close();
//												
//											} catch (IOException ex1) {
//												ex1.printStackTrace();
//											} finally {
//												if (os != null) {
//													os.close();
//												}
//												if (fos != null) {
//													fos.close();
//												}
//
//											}

										}

										cxObj.getQueryObjs().add(obj);
										addTimes++;
									}
								}
							}


						}
					}
					objList.add(cxObj);
				}
			}

			inReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inReader != null) {
					inReader.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (in != null) {
					in.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return objList;
	}

	int sqlQryCount = 0;
	
	/**
	 * parser SQL using JSQLParser
	 * 
	 * @param sql
	 * @throws UnsupportedEncodingException
	 */
	public List<String> parseSql4Table(String sql, File file, String qryName,
			String nodeName, String datasource) throws JSQLParserException,
			UnsupportedEncodingException {
		// logger.info("parsing: file " + file.getName() + " sql " + sql);
//		String originalSql = sql;
//		if (sql.startsWith("--")) {
//			logger.info(originalSql);
//		}
		sqlQryCount++;
		
		
		if (sql.length() == 0 || sql == null || "".equals(sql)) {
			return null;
		}
		// logger.info("original " + sql);
		sql = sql.toUpperCase();
		// 去除注释
		while (sql.contains("/*") && sql.contains("*/")) {
			int bIndex = sql.indexOf("/*");
			int eIndex = sql.indexOf("*/");

			String slashStr = sql.substring(bIndex, eIndex + 2);
			sql = sql.replace(slashStr, "");
		}
		// 去除Cognos prompt的内容
		while (sql.contains("#")) {
			int bIndex = sql.indexOf("#");

			String subStr = sql.substring(bIndex + 1, sql.length());
			int eIndex = subStr.indexOf("#");
			String poundStr = sql.substring(bIndex, eIndex + bIndex + 2);
			if (poundStr.contains(" AND ") || poundStr.contains(" OR ")
					|| poundStr.contains(" AND") || poundStr.contains(" OR")
					|| poundStr.contains("OR ") || poundStr.contains("AND ")) {
				sql = sql.replace(poundStr, "");
			} else {
				sql = sql.replace(poundStr, "(null)");
			}

		}
		
		while (sql.contains("--")) {
			int bIndex = sql.indexOf("--");
			String subStr = sql.substring(bIndex + 1, sql.length());
			int eIndex = subStr.indexOf("\n");
			if(eIndex == -1) {
				eIndex = subStr.length();
			}
			String slashStr = sql.substring(bIndex, eIndex + bIndex + 1);
			sql = sql.replace(slashStr, "");
		}
		sql = sql.replaceAll("< =", "<=");
		sql = sql.replaceAll("> =", ">=");
		sql = sql.replaceAll("< >", "<>");
		sql = sql.replaceAll("! =", "!=");

		// 替换中文文字
		for (int i = 0; i < sql.length(); i++) {
			char ch = sql.charAt(i);
			int v = (int) ch;
			if (v >= 19968 && v <= 171941) {
				sql = sql.replace(ch, 'x');
			}
		}
		sql = sql.replaceAll("\ufffd", ""); // 替换中文问号
		sql = sql.replaceAll("\u3000", ""); // 替换中文空格
		sql = sql.replaceAll("&gt;", ">"); // 替换大于等于
		sql = sql.replaceAll("&lt;", "<"); // 替换小于等于
		// if(sql.indexOf(";") != -1) {
		// sql = sql.substring(0, sql.indexOf(";"));
		// }
		//logger.info("file " + file.getName() + " after replace pound " + sql);
		Statement statement = CCJSqlParserUtil.parse(new String(sql
				.getBytes("UTF-8")));
		TablesNamesFinder finder = new TablesNamesFinder();
		List<String> tableList = null;
		
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
		

		//logger.info("tableList " + tableList);
		sqlCount++;
		return tableList;
	}
	
	public static void main(String[] args) throws Exception {
		
		CognosXmlReader reader = new CognosXmlReader("c:\\pack2_error");
		File file = new File("C:\\cognos\\cognos20\\package2.xml");
		List<CognosXmlObj> list = reader.parseFile(file);
		System.out.println(list);
	}

	public String getOutputFilePath() {
		return outputFilePath;
	}

	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}
}
