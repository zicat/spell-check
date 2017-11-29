package name.zicat.spell.check.biz.test.service.boot;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBException;

import name.zicat.spell.check.biz.cache.ZookeeperLocalCache;
import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.conf.spellcheck.Configuration;
import name.zicat.spell.check.biz.service.zookeeper.register.IndexRegisterService;
import name.zicat.utils.file.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import name.zicat.spell.check.client.model.IndexInfo;
import name.zicat.spell.check.client.model.ServiceInstance;
import name.zicat.spell.check.biz.service.boot.BootService;
import name.zicat.spell.check.biz.service.zookeeper.GlobalIndexVersionService;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.test.dao.zookeeper.ZookeeperServer;

import junit.framework.Assert;

/**
 * 
 * @author lz31
 *
 */
public class BootServiceTest extends ZookeeperServer {
	
	File dir;
	
	@Before
	public void before() throws IOException, NoSuchAlgorithmException, JAXBException, URISyntaxException {
		
		Configuration spellCheckConfiguration = ConfigurationManager.getSpellCheckConfig();
		spellCheckConfiguration.getZookeeperConfig().setHostPort(getHostPort());
		
		dir = new File(spellCheckConfiguration.getIndexConfig().getIndexPath());
		FileUtils.createDirIfNeed(dir);
		FileUtils.cleanUpDir(dir);
		File f = new File(dir, "0");
		f.mkdir();
		f = new File(dir, "1");
		f.mkdir();
		f = new File(dir, "2");
		f.mkdir();
	}
	
	@Test
	public void test() throws Exception {
		
		Configuration spellCheckConfiguration = ConfigurationManager.getSpellCheckConfig();
		String rootPath = spellCheckConfiguration.getZookeeperConfig().getRootPath();
		
		CuratorZookeeperClient zkClient = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		GlobalIndexVersionService service = new GlobalIndexVersionService(zkClient, rootPath);
		
		IndexRegisterService registerService = new IndexRegisterService(zkClient, rootPath);
		IndexInfo index = new IndexInfo(new ServiceInstance("localhost", 8080), 2L);
		registerService.register(index, null);
		index.setVersion(1);
		registerService.register(index, null);
		index.setVersion(0);
		registerService.register(index, null);
		service.setGlobalIndexVersion(2L);
		
		BootService.boot();
		Assert.assertEquals(ZookeeperLocalCache.getCache().getCurrentSpellCheckerEntry().getKey(), Long.valueOf(2));
		Assert.assertTrue(ZookeeperLocalCache.getCache().getSpellChecker(1L) != null);
		Assert.assertTrue(ZookeeperLocalCache.getCache().getSpellChecker(2L) != null);
		Assert.assertTrue(ZookeeperLocalCache.getCache().getSpellChecker(3L) == null);
		
		service.setGlobalIndexVersion(1L);
		Thread.sleep(1000);
		Assert.assertEquals(ZookeeperLocalCache.getCache().getCurrentSpellCheckerEntry().getKey(), Long.valueOf(1));
	}
	
	@After
	public void after() throws Exception {
		
		ZookeeperLocalCache.cleanUp();
		
		FileUtils.cleanUpDir(dir);
		dir.delete();
		
		Configuration spellCheckConfiguration = ConfigurationManager.getSpellCheckConfig();
		String indexPath = spellCheckConfiguration.getIndexConfig().getIndexPath();
		String tempPath = spellCheckConfiguration.getIndexConfig().getTempPath();
		File indexPathFile = new File(indexPath).getCanonicalFile();
		File tempPathFile = new File(tempPath).getCanonicalFile();
		FileUtils.cleanUpDir(tempPathFile);
		FileUtils.cleanUpDir(indexPathFile);
		indexPathFile.delete();
		tempPathFile.delete();
	}

}
