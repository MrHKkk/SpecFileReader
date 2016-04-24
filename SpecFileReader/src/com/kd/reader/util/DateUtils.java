package com.kd.reader.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

private static SimpleDateFormat sdf = new SimpleDateFormat();
	
	
	public static String formatDate(String pattern, Date date) {
		sdf.applyPattern(pattern);
		return sdf.format(date);
	}
	
	public static Date parseStr(String pattern, String dateStr) throws ParseException {
		sdf.applyPattern(pattern);
		Date date = sdf.parse(dateStr);
		return date;
	}
}
