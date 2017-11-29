package name.zicat.spell.check.biz.service.zookeeper;

import java.util.List;
import java.util.Map;

import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;

/**
 * 
 * @author zicat
 *
 */
public class InstancesByIndexVersionService extends AllIndexVersionService {

	public InstancesByIndexVersionService(ZookeeperClient zkClient, String rootPath) {
		super(zkClient, rootPath);
	}
	
	public InstancesByIndexVersionService() throws Exception {
		super();
	}

	/**
	 *
	 * @param version
	 * @return
	 * @throws Exception
	 */
	public List<String> getInstanceByIndexVersion(Long version) throws Exception {
		
		Map<Long, List<String>> indexVersion = getIndexVersions();
		if(indexVersion == null)
			return null;
		
		return indexVersion.get(version);
	}
}
