package com.kd.reader.model;

/**
 * DS文件头对象
 * @author KH
 *
 */
public class DSXmlHeader {
	
	private String instanceId;
	private String serverName;
	private String serverVersion;
	private String dateModified;
	private String fromFile;
	
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public String getServerVersion() {
		return serverVersion;
	}
	public void setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
	}
	public String getDateModified() {
		return dateModified;
	}
	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}
	public String getFromFile() {
		return fromFile;
	}
	public void setFromFile(String fromFile) {
		this.fromFile = fromFile;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((instanceId == null) ? 0 : instanceId.hashCode());
		result = prime * result
				+ ((serverName == null) ? 0 : serverName.hashCode());
		result = prime * result
				+ ((serverVersion == null) ? 0 : serverVersion.hashCode());
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
		final DSXmlHeader other = (DSXmlHeader) obj;
		if (instanceId == null) {
			if (other.instanceId != null)
				return false;
		} else if (!instanceId.equals(other.instanceId))
			return false;
		if (serverName == null) {
			if (other.serverName != null)
				return false;
		} else if (!serverName.equals(other.serverName))
			return false;
		if (serverVersion == null) {
			if (other.serverVersion != null)
				return false;
		} else if (!serverVersion.equals(other.serverVersion))
			return false;
		return true;
	}
	
	
	
}
