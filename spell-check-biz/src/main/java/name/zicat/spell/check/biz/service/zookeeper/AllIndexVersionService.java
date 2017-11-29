package name.zicat.spell.check.biz.service.zookeeper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import name.zicat.spell.check.biz.service.zookeeper.register.IndexRegisterService;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;
import name.zicat.spell.check.client.utils.Constants;

/**
 * 
 * @author zicat
 *
 */
public class AllIndexVersionService extends AbstractZookeeperService {

	public AllIndexVersionService(ZookeeperClient zkClient, String rootPath) {
		super(zkClient, rootPath);
	}
	
	public AllIndexVersionService() throws Exception {
		super();
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<Long, List<String>> getIndexVersions() throws Exception {
		
		String path = IndexRegisterService.getIndexRootPath(rootPath);
		
		if(!zkClient.checkExists(path))
			return null;
		
		List<String> indexs = zkClient.listChildrenNode(path);
		if(indexs == null || indexs.isEmpty())
			return null;
		
		Map<Long, List<String>> results = new HashMap<>();
		for(String version: indexs) {
			Long versionLong = Long.valueOf(version);
			List<String> indexInstances = zkClient.listChildrenNode(path + Constants.SLASH + version);
			if(indexInstances != null && !indexInstances.isEmpty()) {
				results.put(versionLong, indexInstances);
			}
		}
		return results;
	}
}
