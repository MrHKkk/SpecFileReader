package com.ccb.reader.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

public class DbcpBean{
	
	private static BasicDataSource DS;
	
	public DbcpBean(String connectURI, String username, String pswd, String driverClass,
			int maxActive, int maxIdle, int maxWait) {
		initDS(connectURI,username,pswd, driverClass,
				maxActive, maxIdle, maxWait);
	}
	
	public static void initDS(String connectURI, String username, String pswd, String driverClass,
			int maxActive, int maxIdle, int maxWait) {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		ds.setUsername(username);
		ds.setPassword(pswd);
		ds.setUrl(connectURI);
		ds.setMaxActive(maxActive);
		ds.setMaxIdle(maxIdle);
		ds.setMaxWait(maxWait);
		DS = ds;
		System.out.println("初始化连接池...");
	}
	
	public  void shutdown() throws SQLException {
		BasicDataSource bds = DS;
		bds.close();
	}
	
	public  static Connection  getConn() {
		try{
			return DS.getConnection();
		} catch(SQLException ex) {
			ex.printStackTrace();
			System.out.println("连接出错");
			return null;
		}
	}
	
	public static void main(String[] args) {
		DbcpBean db = new DbcpBean("jdbc:oracle:thin:@56.0.98.2:1521:odsbntdb", "odsb", "odsb",
				"oracle.jdbc.driver.OracleDriver", 5, 5, 30000);
		Connection conn = DbcpBean.getConn();
		System.out.println(conn);
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			db.shutdown();
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}
}
