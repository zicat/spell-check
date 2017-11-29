package name.zicat.spell.check.biz.service.zookeeper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import name.zicat.spell.check.biz.service.zookeeper.register.CurrentIndexRegisterService;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;
import name.zicat.spell.check.client.utils.Constants;

/**
 * 
 * @author zicat
 *
 */
public class InstanceCurrentVersionService extends AbstractZookeeperService {

	public InstanceCurrentVersionService() throws Exception {
		super();
	}
	
	public InstanceCurrentVersionService(ZookeeperClient zkClient, String rootPath) {
		super(zkClient, rootPath);
	}
	
	public Map<String, Long> getInstanceCurrentVersion() throws Exception {
		
		if(!zkClient.checkExists(rootPath))
			return null;
		
		List<String> children = zkClient.listChildrenNode(rootPath);
		if(children == null || children.isEmpty())
			return null;
		
		Iterator<String> filter = children.iterator();
		while(filter.hasNext()) {
			String child = filter.next();
			if(!child.startsWith(CurrentIndexRegisterService.NODE_VERSION_)) {
				filter.remove();
			}
		}
		
		if(children.isEmpty())
			return null;
		
		Map<String, Long> result = new HashMap<>();
		for(String child: children) {
			
			String path = rootPath + Constants.SLASH + child;
			Long version = Long.valueOf(new String(zkClient.getData(path), StandardCharsets.UTF_8));
			result.put(child.replace(CurrentIndexRegisterService.NODE_VERSION_, ""), version);
		}
		return result;
	}
}
