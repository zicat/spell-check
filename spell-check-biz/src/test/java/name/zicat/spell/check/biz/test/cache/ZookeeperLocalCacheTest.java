package name.zicat.spell.check.biz.test.cache;

import name.zicat.spell.check.biz.cache.ZookeeperLocalCache;
import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.conf.spellcheck.IndexConfig;
import name.zicat.spell.check.biz.service.zookeeper.GlobalIndexVersionService;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.test.dao.zookeeper.ZookeeperServer;
import name.zicat.spell.check.core.NSpellChecker;
import name.zicat.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author zicat
 */
public class ZookeeperLocalCacheTest {
	
	File localIndexPath = null;
	File tempPath = null;
	ZookeeperServer zkServer = null;
	int timeout = 500000000;
	
	private void initFilePath(String localIndexPathStr, String tempPathStr) throws IOException, NoSuchAlgorithmException, JAXBException, URISyntaxException {
		
		localIndexPath = new File(localIndexPathStr).getCanonicalFile();
		tempPath = new File(tempPathStr).getCanonicalFile();
		FileUtils.createDirIfNeed(localIndexPath);
		FileUtils.cleanUpDir(localIndexPath);
		FileUtils.createDirIfNeed(tempPath);
		FileUtils.cleanUpDir(tempPath);
		
		IndexConfig indexConfig = ConfigurationManager.getSpellCheckConfig().getIndexConfig();
		indexConfig.setIndexPath(localIndexPath.getPath());
		indexConfig.setTempPath(tempPath.getPath());
	}

	@Before
	public void before()  throws Exception {
		initFilePath("./pathIndex", "./pathTemp");
		zkServer = new ZookeeperServer();
		zkServer.initZK();

		String rootPath = ConfigurationManager.getSpellCheckConfig().getZookeeperConfig().getRootPath();
		CuratorZookeeperClient zkClient = new CuratorZookeeperClient(zkServer.getHostPort(), 15000, 15000);
		zkClient.createNode(GlobalIndexVersionService.getPath(rootPath), String.valueOf(100L).getBytes(StandardCharsets.UTF_8));
		ConfigurationManager.getSpellCheckConfig().getZookeeperConfig().setHostPort(zkServer.getHostPort());
	}
	
	@Test
	public void test() throws Exception {
		
		ZookeeperLocalCache cache1 = ZookeeperLocalCache.getCache();
		ZookeeperLocalCache cache2 = ZookeeperLocalCache.getCache();
		Assert.assertTrue(cache1 == cache2);
		
		NSpellChecker checker1 = new NSpellChecker(new RAMDirectory(), new IndexWriterConfig(new StandardAnalyzer()));
		NSpellChecker checker2 = new NSpellChecker(new RAMDirectory(), new IndexWriterConfig(new StandardAnalyzer()));
		NSpellChecker checker3 = new NSpellChecker(new RAMDirectory(), new IndexWriterConfig(new StandardAnalyzer()));
		cache1.setCurrentSpellChecker(1L, checker1);
		cache1.setSpellchecker(1L, checker3);
		Assert.assertTrue(cache1.getCurrentSpellCheckerEntry().getKey() == 1L);
		Assert.assertTrue(cache1.getCurrentSpellCheckerEntry().getValue() == checker1);
		
		cache1.setSpellchecker(2L, checker2);
		Assert.assertTrue(cache1.containsSpellChecker(2L));
		Assert.assertTrue(cache1.containsSpellChecker(1L));
		Assert.assertTrue(cache1.getSpellChecker(2L) == checker2);
		cache1.removeSpellChecker(2L);
		Assert.assertTrue(!cache2.containsSpellChecker(2L));
		cache1.setSpellchecker(2L, checker2);
		ZookeeperLocalCache.cleanUp();
		ZookeeperLocalCache.cleanUp();
		cache1.setSpellchecker(2L, checker2);
	}
	
	@After
	public void after() throws Exception {
		
		ZookeeperLocalCache.cleanUp();
		FileUtils.cleanUpDir(localIndexPath);
		FileUtils.cleanUpDir(tempPath);
		localIndexPath.delete();
		tempPath.delete();
	}
}
