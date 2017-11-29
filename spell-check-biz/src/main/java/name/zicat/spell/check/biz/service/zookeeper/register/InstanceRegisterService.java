package name.zicat.spell.check.biz.service.zookeeper.register;

import name.zicat.spell.check.client.model.ServiceInstance;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;
import name.zicat.spell.check.client.utils.Constants;

/**
 * @author zicat
 * @date 2017/06/21
 */
public class InstanceRegisterService extends AbstractRegisterService<ServiceInstance> {
	
	public static final String LIVE_NODE_PATH = "livenode";
	
    public InstanceRegisterService(ZookeeperClient zkClient, String rootPath) {
    	super(zkClient, rootPath);
    }
    
    public InstanceRegisterService() throws Exception {
    	super();
    }
    
    /**
	 *   Path Struct: ${spell_check_root}/livenode/${index_id}/${ip}:${port}
	 *   Example    :  /spellcheck/livenode/localhost:8080
	 */
	@Override
	public String buildPath(ServiceInstance t) {
		
		return rootPath + Constants.SLASH + LIVE_NODE_PATH + Constants.SLASH + t.getId();
	}
}
