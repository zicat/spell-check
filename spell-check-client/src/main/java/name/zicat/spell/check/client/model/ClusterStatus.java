package name.zicat.spell.check.client.model;

import java.util.List;

/**
 * 
 * @author zicat
 *
 */
public class ClusterStatus {
	
	private List<InstanceStatus> instanceStatus;
	private Long globalIndexVersion;
	
	public ClusterStatus() {
		super();
	}

	public ClusterStatus(Long globalIndexVersion, List<InstanceStatus> instanceStatus) {
		super();
		this.globalIndexVersion = globalIndexVersion;
		this.instanceStatus = instanceStatus;
	}
	
	public List<InstanceStatus> getInstanceStatus() {
		return instanceStatus;
	}

	public void setInstanceStatus(List<InstanceStatus> instanceStatus) {
		this.instanceStatus = instanceStatus;
	}

	public Long getGlobalIndexVersion() {
		return globalIndexVersion;
	}
	
	public void setGlobalIndexVersion(Long globalIndexVersion) {
		this.globalIndexVersion = globalIndexVersion;
	}
}
