package name.zicat.spell.check.biz.service.zookeeper.register;

import name.zicat.spell.check.client.model.ServiceInstance;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;
import name.zicat.spell.check.client.utils.Constants;

/**
 * 
 * @author zicat
 *
 */
public class CurrentIndexRegisterService extends AbstractRegisterService<ServiceInstance> {
	
	public static final String NODE_VERSION_ = "node_version_";
	
	public CurrentIndexRegisterService(ZookeeperClient zkClient, String rootPath) {
		
		super(zkClient, rootPath);
	}
	
	public CurrentIndexRegisterService() throws Exception {
		super();
	}
	
	@Override
	public void register(ServiceInstance t, byte[] data) throws Exception {
		
		if(isRegistered(t)) {
			zkClient.setData(buildPath(t), data);
		} else {
			super.register(t, data);
		}
	}
	/**
	 *   Path Struct: ${spell_check_root}/node_version_${ip}:${port}/${index_id} 
	 *   Example    :  /spellcheck/node_version_localhost:8081/2
	 */
	@Override
	public String buildPath(ServiceInstance t) {
		
		return rootPath + Constants.SLASH + NODE_VERSION_ + t.getId();
	}
}
