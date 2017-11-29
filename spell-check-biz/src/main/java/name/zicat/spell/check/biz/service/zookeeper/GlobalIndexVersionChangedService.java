package name.zicat.spell.check.biz.service.zookeeper;

import java.nio.charset.StandardCharsets;

import name.zicat.spell.check.core.NSpellChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.zicat.spell.check.biz.cache.ZookeeperLocalCache;
import name.zicat.spell.check.biz.service.http.FetchService;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient.WatcherHandler;

/**
 * 
 * @author zicat
 *
 */
public class GlobalIndexVersionChangedService extends GlobalIndexVersionService implements WatcherHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(GlobalIndexVersionChangedService.class);
	
	public GlobalIndexVersionChangedService(ZookeeperClient zkClient, String rootPath) {
		super(zkClient, rootPath);
	}
	
	public GlobalIndexVersionChangedService() throws Exception {
		super();
	}

	/**
	 *
	 * @param path
	 */
	@Override
	public void process(String path) {
		
		try {
			byte[] bs = zkClient.getData(path);
			Long version = Long.valueOf(new String(bs, StandardCharsets.UTF_8));
			FetchService fetchService = new FetchService(version);
			NSpellChecker globalSpellChecker = fetchService.fetch();
			if(globalSpellChecker == null)
				throw new Exception("Fetch GlobalVersion error, SpellChecker is null");
			
			ZookeeperLocalCache.getCache().setCurrentSpellChecker(version, globalSpellChecker);
		} catch (Exception e) {
			LOG.error("changed gloable index error", e);
		}
	}
	
}
