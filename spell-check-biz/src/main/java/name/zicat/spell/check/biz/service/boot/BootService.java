package name.zicat.spell.check.biz.service.boot;

import name.zicat.spell.check.biz.cache.ZookeeperLocalCache;
import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.conf.spellcheck.Configuration;
import name.zicat.spell.check.biz.conf.spellcheck.ZookeeperConfig;
import name.zicat.spell.check.biz.service.http.FetchService;
import name.zicat.spell.check.biz.service.localfile.SCLoadService;
import name.zicat.spell.check.biz.service.zookeeper.GlobalIndexVersionChangedService;
import name.zicat.spell.check.biz.service.zookeeper.register.IndexRegisterService;
import name.zicat.spell.check.client.model.IndexInfo;
import name.zicat.spell.check.client.model.ServiceInstance;
import name.zicat.spell.check.biz.service.zookeeper.GlobalIndexVersionService;
import name.zicat.spell.check.biz.service.zookeeper.register.InstanceRegisterService;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;
import name.zicat.spell.check.core.NSpellChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 
 * @author zicat
 *
 */
public class BootService {

	private static final Logger LOG = LoggerFactory.getLogger(BootService.class);
	/**
	 * boot when service start
	 * @throws Exception
	 */
	public synchronized static void boot() throws Exception {
		
		/** 1. Get Basic Info from Configuration **/
		Configuration spellCheckConfiguration = ConfigurationManager.getSpellCheckConfig();
		ZookeeperConfig zookeeperConfig =  spellCheckConfiguration.getZookeeperConfig();
		int timeout = zookeeperConfig.getSoTimeout();
		ServiceInstance serviceInstance = spellCheckConfiguration.create();
		ZookeeperClient zkClient = new CuratorZookeeperClient(spellCheckConfiguration.getZookeeperConfig().getHostPort(), timeout);
		
		/** 2. Load Local Spell Checker to Local Cache & Register on Zookeeper **/
		SCLoadService spellCheckLoadService = new SCLoadService(spellCheckConfiguration.getIndexConfig().getIndexPath());
		Map<Long, NSpellChecker> spellCheckers = spellCheckLoadService.load();
		
		IndexRegisterService indexRegistryService = new IndexRegisterService();
		if(spellCheckers != null && !spellCheckers.isEmpty()) {
			for(Map.Entry<Long, NSpellChecker> entry: spellCheckers.entrySet()) {
				ZookeeperLocalCache.getCache().setSpellchecker(entry.getKey(), entry.getValue());
				indexRegistryService.register(new IndexInfo(serviceInstance, entry.getKey()), null);
			}
		}
		
		/** 3. Global Current Version **/
		GlobalIndexVersionService globalIndexVersionService = new GlobalIndexVersionService();
		Long globalIndexVersion = globalIndexVersionService.getGlobalIndexVersion();
		if(globalIndexVersion != null) {
			FetchService fetchService = new FetchService(globalIndexVersion);
			try {
				NSpellChecker globalSpellChecker = fetchService.fetch();
				if(globalSpellChecker == null)
					throw new Exception("Fetch GlobalVersion error, SpellChecker is null");
				ZookeeperLocalCache.getCache().setCurrentSpellChecker(globalIndexVersion, globalSpellChecker);
			} catch (Exception e) {
				LOG.error("fail to find global index version from all instance", e);
			}
		}
		zkClient.addNodeDataChangedWatcher(GlobalIndexVersionService.getPath(zookeeperConfig.getRootPath()), new GlobalIndexVersionChangedService());
		
		/** 4. Register Instance */
		InstanceRegisterService instanceRegistryService = new InstanceRegisterService();
		instanceRegistryService.register(serviceInstance, null);
	}
}
