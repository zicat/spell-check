package name.zicat.spell.check.biz.service.zookeeper;

import java.util.List;
import java.util.Random;

import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;
import name.zicat.spell.check.client.utils.Constants;

/**
 * 
 * @author zicat
 *
 */
public class RandomAvailableInstanceByVersion extends InstancesByIndexVersionService {
	
	public static final Random random = new Random();
	
	public RandomAvailableInstanceByVersion() throws Exception {
		super();
	}
	
	public RandomAvailableInstanceByVersion(ZookeeperClient zkClient, String rootPath) {
		super(zkClient, rootPath);
	}

	/**
	 *
	 * @param version
	 * @return
	 * @throws Exception
	 */
	public String random(Long version) throws Exception {
		
		List<String> instances = getInstanceByIndexVersion(version);
		if(instances == null || instances.isEmpty()) {
			throw  new Exception("No available version " + version);
		}
		
		/** Redirect Request to Other Instance **/
		return Constants.HTTP_PREFIX + instances.get(random.nextInt(instances.size()));
	}

}
