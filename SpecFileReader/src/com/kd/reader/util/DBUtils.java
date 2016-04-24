package com.kd.reader.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;





public class DBUtils {

	public static Connection getConnection() {
		Connection conn = null;
		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@56.0.98.2:1521:odsbntdb", "odsb", "odsb");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	public static Connection getHzConnection() {
		Connection conn = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@56.0.98.2:1521:hzadb", "odsb", "odsb");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
		
	}
	
	/**
	 * 获取所有JOB ID
	 * @return
	 */
	public static Map<String, String> getAllJobId() {
		Connection conn = getHzConnection();
		StringBuilder sqlQry = new StringBuilder();
		sqlQry.append("select jobid, jobname, runstatus from job_info");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			pstmt = conn.prepareStatement(sqlQry.toString());
			rs = pstmt.executeQuery();
			while(rs.next()) {
				map.put(rs.getString("jobname"), rs.getString("jobid") + "_" + rs.getString("runstatus"));
			}
		} catch(SQLException ex) {
			ex.printStackTrace();
		} finally {
			closeRs(rs);
			closeSt(pstmt);
			closeConn(conn);
		}
		return map;
	}
	
	public static void closeRs(ResultSet rs) {
		if(rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void closeSt(Statement st) {
		if(st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void closeConn(Connection conn) {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) {
		Connection conn = DBUtils.getHzConnection();
		System.out.println(conn);
		try {
			conn.close();
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
		
	}
	
}
