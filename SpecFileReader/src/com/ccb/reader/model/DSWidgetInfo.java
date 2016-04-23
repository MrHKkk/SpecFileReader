package com.ccb.reader.model;

import java.util.List;

/**
 * DS控件
 * @author 
 *
 */
public class DSWidgetInfo {

	private int stageId;
	private String instId; 
	private String stageIdentifier;
	private List<DSWidgetInfo> outputPins;
	private List<DSWidgetInfo> inputPins;
	private String stageType;
	private String pratnerId;
	private String tableName;
	private String sqlStr;
	private String databaseName;
	private String databaseUser;
	private String dateModified;
	private String widgetName;
	private String outputPinsStr;
	private String inputPinsStr;
	private String jobId; //作业ID
	
	private boolean isParent;
	private String parentId;
	
	
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getOutputPinsStr() {
		return outputPinsStr;
	}
	public void setOutputPinsStr(String outputPinsStr) {
		this.outputPinsStr = outputPinsStr;
	}
	public String getInputPinsStr() {
		return inputPinsStr;
	}
	public void setInputPinsStr(String inputPinsStr) {
		this.inputPinsStr = inputPinsStr;
	}
	public String getWidgetName() {
		return widgetName;
	}
	public void setWidgetName(String widgetName) {
		this.widgetName = widgetName;
	}
	
	public String getInstId() {
		return instId;
	}
	public void setInstId(String instId) {
		this.instId = instId;
	}
	public String getStageIdentifier() {
		return stageIdentifier;
	}
	public void setStageIdentifier(String stageIdentifier) {
		this.stageIdentifier = stageIdentifier;
	}
	public String getStageType() {
		return stageType;
	}
	public void setStageType(String stageType) {
		this.stageType = stageType;
	}
	public String getPratnerId() {
		return pratnerId;
	}
	public void setPratnerId(String pratnerId) {
		this.pratnerId = pratnerId;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getSqlStr() {
		return sqlStr;
	}
	public void setSqlStr(String sqlStr) {
		this.sqlStr = sqlStr;
	}
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	public String getDatabaseUser() {
		return databaseUser;
	}
	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}
	public boolean isParent() {
		return isParent;
	}
	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public void setOutputPins(List<DSWidgetInfo> outputPins) {
		this.outputPins = outputPins;
	}
	public List<DSWidgetInfo> getOutputPins() {
		return outputPins;
	}
	public List<DSWidgetInfo> getInputPins() {
		return inputPins;
	}
	public void setInputPins(List<DSWidgetInfo> inputPins) {
		this.inputPins = inputPins;
	}
	public String getDateModified() {
		return dateModified;
	}
	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}
	public int getStageId() {
		return stageId;
	}
	public void setStageId(int stageId) {
		this.stageId = stageId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instId == null) ? 0 : instId.hashCode());
		result = prime * result + stageId;
		result = prime * result
				+ ((stageIdentifier == null) ? 0 : stageIdentifier.hashCode());
		result = prime * result
				+ ((stageType == null) ? 0 : stageType.hashCode());
		result = prime * result
				+ ((widgetName == null) ? 0 : widgetName.hashCode());
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
		final DSWidgetInfo other = (DSWidgetInfo) obj;
		if (instId == null) {
			if (other.instId != null)
				return false;
		} else if (!instId.equals(other.instId))
			return false;
		if (stageId != other.stageId)
			return false;
		if (stageIdentifier == null) {
			if (other.stageIdentifier != null)
				return false;
		} else if (!stageIdentifier.equals(other.stageIdentifier))
			return false;
		if (stageType == null) {
			if (other.stageType != null)
				return false;
		} else if (!stageType.equals(other.stageType))
			return false;
		if (widgetName == null) {
			if (other.widgetName != null)
				return false;
		} else if (!widgetName.equals(other.widgetName))
			return false;
		return true;
	}
		
	
	

	
	
	
	
}
