package com.ccb.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.List;

import com.ccb.reader.model.CognosXmlObj;
import com.ccb.reader.util.DBUtils;

public class Test {

	public static void main(String[] args) throws Exception {
		Connection conn = DBUtils.getConnection();
		CognosXmlReader xmlReader = new CognosXmlReader();
		List<CognosXmlObj> list = xmlReader.findAllCognosXmlObj(conn);
		System.out.println(list);
	}
	
	
	
}
