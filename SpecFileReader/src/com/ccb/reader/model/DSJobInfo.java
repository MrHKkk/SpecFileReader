package com.ccb.reader.model;

import java.util.List;

/**
 * DS作业信息
 * @author KH
 *
 */
public class DSJobInfo {

	private String dsInstId;
	private String jobId;
	private String identListStr;
	private String jobIdentifier;
	private String dateModified;
	private String jobCategory;
	private String stageListStr;
	private String runStatus;

	private List<String> identList;
	private List<String> stageList;
	
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getIdentListStr() {
		return identListStr;
	}
	public void setIdentListStr(String identListStr) {
		this.identListStr = identListStr;
	}
	public String getJobIdentifier() {
		return jobIdentifier;
	}
	public void setJobIdentifier(String jobIdentifier) {
		this.jobIdentifier = jobIdentifier;
	}
	public String getDateModified() {
		return dateModified;
	}
	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}
	public String getJobCategory() {
		return jobCategory;
	}
	public void setJobCategory(String jobCategory) {
		this.jobCategory = jobCategory;
	}
	public String getStageListStr() {
		return stageListStr;
	}
	public void setStageListStr(String stageListStr) {
		this.stageListStr = stageListStr;
	}
	public List<String> getIdentList() {
		return identList;
	}
	public void setIdentList(List<String> identList) {
		this.identList = identList;
	}
	public List<String> getStageList() {
		return stageList;
	}
	public void setStageList(List<String> stageList) {
		this.stageList = stageList;
	}
	public String getDsInstId() {
		return dsInstId;
	}
	public void setDsInstId(String dsInstId) {
		this.dsInstId = dsInstId;
	}
	public String getRunStatus() {
		return runStatus;
	}
	public void setRunStatus(String runStatus) {
		this.runStatus = runStatus;
	}
	
	
	
	
}
