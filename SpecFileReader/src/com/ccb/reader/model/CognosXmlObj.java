package com.ccb.reader.model;

import java.util.List;

public class CognosXmlObj {

	private String name;
	private String id;
	private String path;
	private	List<SqlQueryObj> queryObjs;
	private String reportName;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public List<SqlQueryObj> getQueryObjs() {
		return queryObjs;
	}
	public void setQueryObjs(List<SqlQueryObj> queryObjs) {
		this.queryObjs = queryObjs;
	}
	public String getReportName() {
		return reportName;
	}
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	public String toString() {
		return "reportName: " + reportName + " id: " + id ; 
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((queryObjs == null) ? 0 : queryObjs.hashCode());
		result = prime * result
				+ ((reportName == null) ? 0 : reportName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CognosXmlObj other = (CognosXmlObj) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (queryObjs == null) {
			if (other.queryObjs != null)
				return false;
		} else if (!queryObjs.equals(other.queryObjs))
			return false;
		if (reportName == null) {
			if (other.reportName != null)
				return false;
		} else if (!reportName.equals(other.reportName))
			return false;
		return true;
	}
	
	
	

	
}
