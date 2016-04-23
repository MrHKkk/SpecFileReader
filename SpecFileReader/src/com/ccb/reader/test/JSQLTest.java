package com.ccb.reader.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;



import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class JSQLTest {
	
	
	private static String existsSql = "select * from DW_SXKH_RPT_JCMX a LEFT JOIN   dw_bmis_dim_inst_info b on a.inst_no=b.inst_no where "
		+ " a.date_id=replace(replace(prompt('p_date','string'),'年',''),'月','') "
		+ " and exists ( select 1  from  DW_SXKH_RPT_JCMX c, dw_bmis_dim_inst_info b where c.inst_no = b.inst_no "
		+ " and c.LN_bal >0 )";

	
	private static String sql3 = "SELECT T.INST_NO ,T.ACCT_NO ,T.CURR_CD ,T.CURR_IDEN ,T.CUST_OPEN_DT ,T.CUST_NO ,T.CUST_NAME ,T.SUBJECT ,T.TLR_NO ,T.ACQU ,'LYEZH' AS ETL_TYPE ,'2015-01-10' AS ETL_DATE FROM ODSBDATA.BF_AGT_DEP_ACCT_SAE@ODSBPTDB T WHERE REGEXP_LIKE(T.SUBJECT,'^(2244)') AND CUST_OPEN_DT > = '2015-01-01' ";

	private static String replacePound(String sql) {
		String sqlStr;
		sql = sql.toUpperCase();
		while(sql.contains("#")) {
			int bIndex = sql.indexOf("#");
			
			String subStr = sql.substring(bIndex + 1, sql.length());
			int eIndex = subStr.indexOf("#");
			String poundStr = sql.substring(bIndex, eIndex + bIndex + 2);
			if(poundStr.contains(" AND ") || poundStr.contains(" OR ") ) {
				sql = sql.replace(poundStr, "");
			} else {
				sql = sql.replace(poundStr, "(null)");
			}
			
		}
		sqlStr = sql;

		return sqlStr;
	}

	
	public static void main(String[] args) throws JSQLParserException, IOException {

			CCJSqlParserManager parserManager = new CCJSqlParserManager();
			//String sql = "select p1.inst_name, p1.bran_cd2 bran_cd, p1.bran_name2, p3.* from dw_dim_inst_info@odsbrun2 p1 inner join ( select p2.out_crd_instn_cd  from DW_AGT_CRD_CRT_BE_RPT p2 where p2.etl_date=''  ) p3 on p1.inst_no=p3.out_crd_instn_cd where (1 = CASE WHEN '440000000' = '' THEN 1 ELSE 0 END OR (p1.inst_no = '' OR p1.bran_cd2 = '' ));";
			
//			String sql = "SELECT t1.*, (SELECT CUST_CERT_NO  FROM DW_PFD_VIP_CUST_INFO WHERE CUST_NAME = '王强') CNO, "
//				+ "(SELECT 1 FROM DUAL) dual, (SELECT 2 FROM DUAL) dual FROM DW_PFD_VIP_INFO t1 "
//				+ "WHERE cust_no = (SELECT CUST_NO FROM DW_PFD_VIP_CUST_INFO_DTL where cust_no = 'CCB123') "
//				+ "AND cust_name = (SELECT CUST_NAME FROM DW_PFD_VIP_CUST_INFO_DTL WHERE CUST_NAME = '王强') "
//				+ "AND mgr_no = (SELECT UAAP_ID FROM DW_PFD_VIP_CUST_INFO WHERE UAAP_ID = 'ADMIN.GD') AND T.ABC = '123' AND T.BCD = '234'"
//				+ "AND (SELECT INST_NO FROM DW_INST_1 WHERE MGR_NO = '123') = (SELECT INST_NO FROM DW_INST_BASE WHERE MGR_NO = '123') "
//				+ "OR (cust_no = (SELECT A FROM DW_INST WHERE B = '1') or cust_no = '4400000')";
			
//			String sql = "SELECT CASE WHEN 1 + 3 > (SELECT 1 FROM DUAL) THEN 0 WHEN 1 + 2 > (SELECT A FROM TAB_B) THEN 2 ELSE 1000 + 1 END AS a FROM B WHERE  (A IN (SELECT B FROM TAB) ) and (1 = CASE WHEN '440000000' = (SELECT INST_NO FROM DW_INST)  THEN 1 ELSE 0 END OR (p1.inst_no = prompt('p_company', 'string'))) " ;
//			
//			//String sql = "SELECT CASE WHEN 1 + 3 > 20 THEN 0 ELSE 1000 + 1 END AS D FROM DUAL"; 
//			
//			//String sql = "SELECT A FROM B WHERE (P1 = P2 OR P3 = P4 OR (P5 = P6 OR P7 = P8) AND P9 = P10)";
//			
			InputStream is = new FileInputStream("c:\\sql\\test.sql");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			StringBuilder sb = new StringBuilder();
			
			
			
			String str = null;
			while((str = reader.readLine()) != null) {
				sb.append(str + " ");
			}
			String sql1 = sb.toString();
			
			String newSql = replacePound(sql1);
			
			
			
			
			System.out.println(newSql);
			
			for(int i = 0; i < newSql.length(); i++) {
				char ch = newSql.charAt(i);
				int v = (int)ch;
				if(v >= 19968 && v <= 171941) {
					newSql = newSql.replace(ch, 'x');
				}
			}
			System.out.println(newSql);
			newSql = newSql.replaceAll("\ufffd", "");
			newSql = newSql.replaceAll("\u3000", "");
			parserManager.parse(new StringReader(newSql));
			
			
			System.out.println("original " + newSql);
			Statement statement = CCJSqlParserUtil.parse(newSql);
			Select selectStatement = (Select) statement;
			TablesNamesFinder finder = new TablesNamesFinder();
			List<String> tableList = finder.getTableList(selectStatement);
			System.out.println(tableList);
//			
//			//Select existsSelect = (Select)parserManager.parse(new StringReader(existsSql));
//			
//			
//			Select select = (Select)parserManager.parse(new StringReader(sql));
//			PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
//			Expression expr = plainSelect.getWhere();
//			
//			Statement statement = CCJSqlParserUtil.parse(sql);
//			Select selectStatement = (Select) statement;
//			TablesNamesFinder finder = new TablesNamesFinder();
//			List<String> tableList = finder.getTableList(selectStatement);
//			
//			Statement statement1 = CCJSqlParserUtil.parse(existsSql);
//			Select selectStatement1 = (Select) statement1;
//			TablesNamesFinder finder1 = new TablesNamesFinder();
//			List<String> tableList2 = finder1.getTableList(selectStatement1);
//			
//			
//			//List<String> tableList1 = finder.getTableList(expr);
//			
//			
//			
//			System.out.println(tableList);
//			System.out.println(tableList2);
			
			//List<String> tableList1 = finder.getTableList(selectStatement);
			
	}
	
}
