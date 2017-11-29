package name.zicat.spell.check.biz.service.zookeeper;

import java.util.List;

import name.zicat.spell.check.biz.service.zookeeper.register.InstanceRegisterService;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;
import name.zicat.spell.check.client.utils.Constants;

/**
 * 
 * @author zicat
 *
 */
public class AvailableInstanceService extends AbstractZookeeperService {

	public AvailableInstanceService() throws Exception {
		super();
	}
	
	public AvailableInstanceService(ZookeeperClient zkClient, String rootPath) {
		super(zkClient, rootPath);
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public List<String> getInstance() throws Exception {
		
		String path = rootPath + Constants.SLASH + InstanceRegisterService.LIVE_NODE_PATH;
		if(!zkClient.checkExists(path))
			return null;
		
		return zkClient.listChildrenNode(path);
	}

}
