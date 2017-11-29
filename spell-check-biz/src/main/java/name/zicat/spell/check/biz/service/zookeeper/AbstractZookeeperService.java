package name.zicat.spell.check.biz.service.zookeeper;

import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.conf.spellcheck.ZookeeperConfig;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;

/**
 * 
 * @author zicat
 *
 */
public abstract class AbstractZookeeperService {
	
	protected final ZookeeperClient zkClient;
	protected final String rootPath;
	
	public AbstractZookeeperService(ZookeeperClient zkClient, String rootPath) {

		this.zkClient = zkClient;
		this.rootPath = rootPath;
	}

	/**
	 *
	 * @throws Exception
	 */
	public AbstractZookeeperService() throws Exception {
		
		ZookeeperConfig zookeeperConfig = ConfigurationManager.getSpellCheckConfig().getZookeeperConfig();
		this.zkClient = new CuratorZookeeperClient(zookeeperConfig.getHostPort(), zookeeperConfig.getSoTimeout());
		this.rootPath = zookeeperConfig.getRootPath();
	}
}
