package com.kd.reader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kd.reader.db.DbcpBean;
import com.kd.reader.model.DSJobInfo;
import com.kd.reader.model.DSWidgetInfo;
import com.kd.reader.model.DSXmlHeader;
import com.kd.reader.util.DBUtils;
import com.kd.reader.util.DateUtils;

public class DSObjPersistence {

	
	private static String pattern = "yyyy-MM-dd HH:mm:ss";
	
	
	private static Object lock = new Object();
	
	public static void persistData(DSXmlHeader header, Map<DSWidgetInfo, List<DSWidgetInfo>> map, List<DSJobInfo> list, String sameFile) {
		Connection conn = null;
		try {
		
			conn = DbcpBean.getConn();
			conn.setAutoCommit(false);
			if("1".equals(sameFile)) {
				//对于同一个文件拆分出来子文件，都有完整的DS XML文件结构顺序，所以需要判断是否会重复插入多个Header
				synchronized (lock) {
					if(GlobalConst.headerFlag) {
						persistDSHeader(header, conn);
						GlobalConst.headerFlag = false;
					}
				}
			} else {
				persistDSHeader(header, conn);
			}
			persistDSJobs(list, header, conn);
			persistDSWidget(map, header, conn);
			conn.commit();
			conn.setAutoCommit(true);
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			DBUtils.closeConn(conn);
		}
	}
	
	public static void persistDSHeader(DSXmlHeader header, Connection conn) {
		
		StringBuilder sqlQry = new StringBuilder();
		PreparedStatement pstmt = null;
		sqlQry.append("INSERT INTO ds_host_info (INSTID, SERVER_NAME, INSTANCE_ID, UPTIME, INSERT_DATE, SPLIT_FILE_FOLDER)");
		sqlQry.append(" VALUES (SEQ_DS_HOST_ID.NEXTVAL,?,?,?,?,?)");
		try {
			pstmt = conn.prepareStatement(sqlQry.toString());
			
			pstmt.setString(1, header.getServerName());
			pstmt.setString(2, header.getInstanceId());
			pstmt.setString(3, header.getDateModified());
			Date date = new Date(System.currentTimeMillis());
			pstmt.setString(4, DateUtils.formatDate(pattern, date));
			pstmt.setString(5, header.getFromFile());
			pstmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtils.closeSt(pstmt);
		}
	}
	
	/**
	 * 
	 * 
	 * @param map
	 * @param header
	 * @param conn
	 */
	public static void persistDSWidget(Map<DSWidgetInfo, List<DSWidgetInfo>> map, DSXmlHeader header, Connection conn) {
		StringBuilder sqlQry = new StringBuilder();
		PreparedStatement pstmt = null;
		sqlQry.append("INSERT INTO DS_STAGES_INFO (STAGE_ID, INST_ID, STAGE_IDENTIFIER, STAGE_NAME, INPUTPINS, ")
			.append("OUTPUTPINS, STAGE_TYPE, UPDATE_TIME, PARTNER_ID, TABLENAME, SQL_STR, DBNAME_STR, DBUSER_STR, INSERT_DATE)")
			.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		
		
		Set<DSWidgetInfo> keySet = map.keySet();
		Iterator<DSWidgetInfo> iter = keySet.iterator();
		try {
			
			pstmt = conn.prepareStatement(sqlQry.toString());
			while(iter.hasNext()) {
				DSWidgetInfo widget = iter.next();
				
				pstmt.setInt(1, widget.getStageId());
				pstmt.setString(2, widget.getInstId());
				pstmt.setString(3, widget.getStageIdentifier());
				pstmt.setString(4, widget.getWidgetName());
				pstmt.setString(5, widget.getInputPinsStr());
				pstmt.setString(6, widget.getOutputPinsStr());
				pstmt.setString(7, widget.getStageType());
				pstmt.setString(8, widget.getDateModified());
				pstmt.setString(9, widget.getPratnerId());
				pstmt.setString(10, widget.getTableName());
				pstmt.setString(11, widget.getSqlStr());
				pstmt.setString(12, widget.getDatabaseName());
				pstmt.setString(13, widget.getDatabaseUser());
				Date date = new Date(System.currentTimeMillis());
				pstmt.setString(14, DateUtils.formatDate(pattern, date));
				pstmt.addBatch();
				
				List<DSWidgetInfo> list = map.get(widget);
				if(!list.isEmpty()) {
					for(DSWidgetInfo subWidget : list) {
						pstmt.setString(1, String.valueOf(subWidget.getStageId()));
						pstmt.setString(2, widget.getInstId());
						pstmt.setString(3, subWidget.getStageIdentifier());
						pstmt.setString(4, subWidget.getWidgetName());
						pstmt.setString(5, subWidget.getInputPinsStr());
						pstmt.setString(6, subWidget.getOutputPinsStr());
						pstmt.setString(7, subWidget.getStageType());
						pstmt.setString(8, subWidget.getDateModified());
						pstmt.setString(9, subWidget.getPratnerId());
						pstmt.setString(10, subWidget.getTableName());
						pstmt.setString(11, subWidget.getSqlStr());
						pstmt.setString(12, subWidget.getDatabaseName());
						pstmt.setString(13, subWidget.getDatabaseUser());
						pstmt.setString(14, DateUtils.formatDate(pattern, date));
						pstmt.addBatch();
					}
				}
			}
			pstmt.executeBatch();
			pstmt.clearBatch();
			
			
		} catch(SQLException ex) {
			ex.printStackTrace();
		} finally {
			if(pstmt != null) {
				DBUtils.closeSt(pstmt);
			}
		}
	}
	
	/**
	 * 
	 * @param list
	 * @param header
	 * @param conn
	 */
	public static void persistDSJobs(List<DSJobInfo> list, DSXmlHeader header, Connection conn) {
		StringBuilder sqlQry = new StringBuilder();
		PreparedStatement pstmt = null;
		sqlQry.append("INSERT INTO DS_JOBS_INFO(JOB_ID, INST_ID, JOB_NAME, JOB_UPTIME, JOB_CATEGORY, ")
			.append(" JOB_CONTAINID, STAGE_LIST, STAGECODE_LIST, JOB_STATUS, INSERT_DATE) VALUES (?,?,?,?,?,?,?,?,?,?) ");
		
		try {
			
			pstmt = conn.prepareStatement(sqlQry.toString());
			for(DSJobInfo job : list) {
				pstmt.setString(1, job.getJobId());
				pstmt.setString(2, job.getDsInstId());
				pstmt.setString(3, job.getJobIdentifier());
				pstmt.setString(4, job.getDateModified());
				pstmt.setString(5, job.getJobCategory());
				pstmt.setString(6, null);
				pstmt.setString(7, job.getStageListStr());
				pstmt.setString(8, job.getIdentListStr());
				pstmt.setString(9, job.getRunStatus());
				Date date = new Date(System.currentTimeMillis());
				pstmt.setString(10, DateUtils.formatDate(pattern, date));
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			pstmt.clearBatch();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(pstmt != null) {
				DBUtils.closeSt(pstmt);
			}
		}
	}
	
}
