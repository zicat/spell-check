package name.zicat.spell.check.client.model;

import java.util.List;

/**
 * 
 * @author zicat
 *
 */
public class InstanceStatus {
	
	private String instanceId;
	private Long currentVersion;
	private List<Long> availableVersions;
	
	public InstanceStatus() {
		super();
	}

	public Long getCurrentVersion() {
		return currentVersion;
	}
	
	public void setCurrentVersion(Long currentVersion) {
		this.currentVersion = currentVersion;
	}
	
	public List<Long> getAvailableVersions() {
		return availableVersions;
	}
	
	public void setAvailableVersions(List<Long> availableVersions) {
		this.availableVersions = availableVersions;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
}
