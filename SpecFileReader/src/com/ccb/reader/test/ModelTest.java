package com.ccb.reader.test;

import java.util.ArrayList;
import java.util.List;

import com.ccb.reader.model.CognosXmlObj;
import com.ccb.reader.model.SqlQueryObj;

public class ModelTest {

	
	public static void main(String[] args) {
		
		CognosXmlObj cognosObj = new CognosXmlObj();
		cognosObj.setId("iEF7CFDA9AB58468FABEF0663E63732CA");
		cognosObj.setReportName("按国家统计个人非居民存款信息");
//		List<SqlQueryObj> list = new ArrayList<SqlQueryObj>();
//		cognosObj.setQueryObjs(list);
		
		
		CognosXmlObj cognosObj1 = new CognosXmlObj();
		cognosObj1.setId("iEF7CFDA9AB58468FABEF0663E63732CA");
		cognosObj1.setReportName("按国家统计个人非居民存款信息");
		System.out.println(cognosObj.equals(cognosObj1));
		
		List<CognosXmlObj> objList = new ArrayList<CognosXmlObj>();
		objList.add(cognosObj);
		System.out.println(objList.contains(cognosObj1));
	}
}
