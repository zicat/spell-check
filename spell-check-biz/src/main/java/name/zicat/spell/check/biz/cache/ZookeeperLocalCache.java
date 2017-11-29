package name.zicat.spell.check.biz.cache;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import name.zicat.spell.check.core.NSpellChecker;
import name.zicat.utils.ds.map.CommonEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.util.IOUtils;
import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.service.zookeeper.register.CurrentIndexRegisterService;
import name.zicat.spell.check.biz.service.zookeeper.register.IndexRegisterService;
import name.zicat.spell.check.client.model.IndexInfo;
import name.zicat.spell.check.client.model.ServiceInstance;

/**
 * 
 * @author zicat
 *
 */
public class ZookeeperLocalCache {
	
	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperLocalCache.class);
	
	private final Map<Long, NSpellChecker> spellCheckerCache = new ConcurrentHashMap<>();
	private volatile Map.Entry<Long, NSpellChecker> currentSpellChecker;
	private final BlockingQueue<Long> registerWork = new LinkedBlockingDeque<>(30);
	private Thread workThread;
	private volatile boolean closed = false;
	
	private static final ZookeeperLocalCache instance = new ZookeeperLocalCache();

	private ZookeeperLocalCache() {
		workThread = new Thread(() -> {

            while(!closed) {
                try {
                    Long version = registerWork.take();
                    ServiceInstance serviceInstance = ConfigurationManager.getSpellCheckConfig().create();
                    CurrentIndexRegisterService cureentIndexRegisterService = new CurrentIndexRegisterService();
                    cureentIndexRegisterService.register(serviceInstance, String.valueOf(version).getBytes(StandardCharsets.UTF_8));

                    IndexRegisterService indexRegistryService = new IndexRegisterService();
                    IndexInfo indexInfo = new IndexInfo(serviceInstance, version);
                    if(!indexRegistryService.isRegistered(indexInfo)) {
                        indexRegistryService.register(new IndexInfo(serviceInstance, version), null);
                    }
                } catch (Exception e) {
                    LOG.error("register work error", e);
                }
            }

        });
		workThread.start();
	}
	
	/**
	 * 
	 * @return
	 */
	public Map.Entry<Long, NSpellChecker> getCurrentSpellCheckerEntry() {
		return currentSpellChecker;
	}

	/**
	 *
	 * @return
	 */
	public NSpellChecker getCurrentSpellChecker() {
		
		final Map.Entry<Long, NSpellChecker> entry = getCurrentSpellCheckerEntry();
		return entry == null? null: entry.getValue();
	}
	
	
	
	/**
	 * 
	 * @param currentVersion
	 * @param currentSpellChecker
	 */
	public synchronized void setCurrentSpellChecker(final Long currentVersion, final NSpellChecker currentSpellChecker) {
		this.currentSpellChecker = new CommonEntry<>(currentVersion, currentSpellChecker);
		setSpellchecker(currentVersion, currentSpellChecker);
		registerWork.offer(currentVersion);
	}

	/**
	 *
	 * @param version
	 * @param spellChecker
	 */
	public void setSpellchecker(Long version, NSpellChecker spellChecker) {
		spellCheckerCache.put(version, spellChecker);
	}

	/**
	 *
	 * @param version
	 * @return
	 */
	public NSpellChecker removeSpellChecker(Long version) throws Exception {
		
		NSpellChecker nSpellChecker = spellCheckerCache.remove(version);
		ServiceInstance serviceInstance = ConfigurationManager.getSpellCheckConfig().create();
		IndexRegisterService indexRegistryService = new IndexRegisterService();
		IndexInfo indexInfo = new IndexInfo(serviceInstance, version);
		try {
			if(indexRegistryService.isRegistered(indexInfo)) {
				indexRegistryService.cancell(indexInfo);
			}
		} catch(Exception e) {
			spellCheckerCache.put(version, nSpellChecker);
			throw new Exception("Remove Version " + version + " Fail", e);
		}
		return nSpellChecker;
	}

	/**
	 *  For Testing
	 */
	public synchronized static void cleanUp() throws Exception {
		
		if(ZookeeperLocalCache.getCache().currentSpellChecker != null) {
			NSpellChecker current = ZookeeperLocalCache.getCache().currentSpellChecker.getValue();
			NSpellChecker currentInCache = ZookeeperLocalCache.getCache().getSpellChecker(ZookeeperLocalCache.getCache().currentSpellChecker.getKey());
			if(currentInCache != null && currentInCache != current) {
				IOUtils.close(currentInCache);
				ZookeeperLocalCache.getCache().removeSpellChecker(ZookeeperLocalCache.getCache().currentSpellChecker.getKey());
			}
			IOUtils.close(current);
			ZookeeperLocalCache.getCache().currentSpellChecker = null;
		}
		
		for(Map.Entry<Long, NSpellChecker> entry: ZookeeperLocalCache.getCache().spellCheckerCache.entrySet()) {
			IOUtils.close(entry.getValue());
		}
		
		ZookeeperLocalCache.getCache().spellCheckerCache.clear();
	}
	
	/**
	 * 
	 * @param version
	 * @return
	 */
	public boolean containsSpellChecker(Long version) {
		return spellCheckerCache.containsKey(version);
	}
	
	/**
	 * 
	 * @param version
	 * @return
	 */
	public NSpellChecker getSpellChecker(Long version) {
		return spellCheckerCache.get(version);
	}
	
	/**
	 * 
	 * @return
	 */
	public static ZookeeperLocalCache getCache() {
		return instance;
	}
}
