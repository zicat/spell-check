package name.zicat.spell.check.biz.test.service.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBException;

import name.zicat.spell.check.biz.service.zookeeper.GlobalIndexVersionService;
import name.zicat.spell.check.biz.cache.ZookeeperLocalCache;
import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.conf.spellcheck.IndexConfig;
import name.zicat.spell.check.biz.service.http.FetchService;
import name.zicat.spell.check.biz.test.rest.SpellCheckServer;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.query.*;
import name.zicat.spell.check.core.NSpellChecker;
import name.zicat.spell.check.core.utils.SpellCheckResultSplit;
import name.zicat.utils.file.FileUtils;
import org.apache.lucene.search.spell.SuggestMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import name.zicat.spell.check.client.model.ClusterStatus;
import name.zicat.spell.check.client.model.InstanceStatus;
import name.zicat.spell.check.client.model.Response;
import name.zicat.spell.check.client.model.SpellCheckModel;
import name.zicat.spell.check.client.test.dao.zookeeper.ZookeeperServer;

import junit.framework.Assert;

/**
 * 
 * @author zicat
 *
 */
public class UploadServiceTest {

	File localIndexPath = null;
	File tempPath = null;
	
	String dictionary1 = "samsung ssds\u0001100";
	String dictionary2 = "samsung ssda\u0001100";
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
	public void before() throws Exception {
		
		initFilePath("./pathIndex", "./pathTemp");
		zkServer = new ZookeeperServer();
		zkServer.initZK();

		String rootPath = ConfigurationManager.getSpellCheckConfig().getZookeeperConfig().getRootPath();
		CuratorZookeeperClient zkClient = new CuratorZookeeperClient(zkServer.getHostPort(), 15000, 15000);
		zkClient.createNode(GlobalIndexVersionService.getPath(rootPath), String.valueOf(100L).getBytes(StandardCharsets.UTF_8));
		ConfigurationManager.getSpellCheckConfig().getZookeeperConfig().setHostPort(zkServer.getHostPort());
		new SpellCheckServer().start();
	}
	
	public InputStream build(String dictionary) {
		InputStream in = new ByteArrayInputStream(dictionary.getBytes(StandardCharsets.UTF_8));
		return in;
	}
	
	@Test
	public void uploadTest() throws Exception {

		PingRequest pingRequest = new PingRequest("http://localhost:" + ConfigurationManager.getSpellCheckConfig().getServicePort(), timeout);
		Response<String> pingResponse = pingRequest.ping();
		Assert.assertEquals(pingResponse.getCode(), 500);

		UploadRequest request = new UploadRequest("http://localhost:" + ConfigurationManager.getSpellCheckConfig().getServicePort(), timeout);
		Response<String> response = request.upload(1L, build(dictionary1));
		Assert.assertEquals(response.getCode(), 200);
		Assert.assertNotNull(response.getMessage());
		
		response = request.upload(2L, build(dictionary2));
		Assert.assertEquals(response.getCode(), 200);
		Assert.assertNotNull(response.getMessage());
		
		response = request.upload(1L, build(dictionary2));
		Assert.assertEquals(response.getCode(), 500);
		
		SpellCheckRequest spellCheckRequest = new SpellCheckRequest("http://localhost:" + ConfigurationManager.getSpellCheckConfig().getServicePort(), timeout);
		Response<SpellCheckModel> spellCheckResponse = spellCheckRequest.spellCheck("samsung ssd", 2, 1L);
		Assert.assertEquals(spellCheckResponse.getCode(), 200);
		Assert.assertEquals(SpellCheckResultSplit.split(spellCheckResponse.getBody().getSuggestions().get(0)), "samsung ssds");
		
		spellCheckResponse = spellCheckRequest.spellCheck("samsung ssd", 2, null);
		Assert.assertEquals(spellCheckResponse.getCode(), 500);
		
		SwapRequest swapRequest = new SwapRequest("http://localhost:" + ConfigurationManager.getSpellCheckConfig().getServicePort(), timeout);
		Response<String> swapResponse = swapRequest.swap(2L);
		Assert.assertEquals(swapResponse.getCode(), 200);
		Thread.sleep(500);
		
		spellCheckResponse = spellCheckRequest.spellCheck("samsung ssd", 2, null);
		Assert.assertEquals(spellCheckResponse.getCode(), 200);
		Assert.assertEquals(SpellCheckResultSplit.split(spellCheckResponse.getBody().getSuggestions().get(0)), "samsung ssda");
		
		spellCheckResponse = spellCheckRequest.spellCheck("samsung ssd", 2, 2L);
		Assert.assertEquals(spellCheckResponse.getCode(), 200);
		Assert.assertEquals(SpellCheckResultSplit.split(spellCheckResponse.getBody().getSuggestions().get(0)), "samsung ssda");
		
		spellCheckResponse = spellCheckRequest.spellCheck("samsung ssd", 2, 1L);
		Assert.assertEquals(spellCheckResponse.getCode(), 200);
		Assert.assertEquals(SpellCheckResultSplit.split(spellCheckResponse.getBody().getSuggestions().get(0)), "samsung ssds");
		
		swapResponse = swapRequest.swap(1L);
		Thread.sleep(500);
		Assert.assertEquals(swapResponse.getCode(), 200);
		
		spellCheckResponse = spellCheckRequest.spellCheck("samsung ssd", 2, null);
		Assert.assertEquals(spellCheckResponse.getCode(), 200);
		Assert.assertEquals(SpellCheckResultSplit.split(spellCheckResponse.getBody().getSuggestions().get(0)), "samsung ssds");
		
		spellCheckResponse = spellCheckRequest.spellCheck("samsung ssd", 2, 2L);
		Assert.assertEquals(spellCheckResponse.getCode(), 200);
		Assert.assertEquals(SpellCheckResultSplit.split(spellCheckResponse.getBody().getSuggestions().get(0)), "samsung ssda");
		
		spellCheckResponse = spellCheckRequest.spellCheck("samsung ssd", 2, 1L);
		Assert.assertEquals(spellCheckResponse.getCode(), 200);
		Assert.assertEquals(SpellCheckResultSplit.split(spellCheckResponse.getBody().getSuggestions().get(0)), "samsung ssds");
		
		swapResponse = swapRequest.swap(3L);
		Thread.sleep(500);
		Assert.assertEquals(swapResponse.getCode(), 500);

		pingResponse = pingRequest.ping();
		Assert.assertEquals(pingResponse.getCode(), 200);

		pingResponse = pingRequest.pingTurnOff();
		Assert.assertEquals(pingResponse.getCode(), 200);

		pingResponse = pingRequest.ping();
		Assert.assertEquals(pingResponse.getCode(), 500);

		pingResponse = pingRequest.pingTurnOn();
		Assert.assertEquals(pingResponse.getCode(), 200);

		pingResponse = pingRequest.ping();
		Assert.assertEquals(pingResponse.getCode(), 200);

		File pathIndex2 = new File("./pathIndex2");
		File pathTemp2 = new File("./pathTemp2");
		try {
			FileUtils.createDirIfNeed(pathIndex2);
			FileUtils.createDirIfNeed(pathTemp2);
			FileUtils.cleanUpDir(pathIndex2);
			FileUtils.cleanUpDir(pathTemp2);
			FetchService fetchService = new FetchService(1L, "./pathTemp2", "./pathIndex2");
			NSpellChecker nSpellChecker = fetchService.fetch();
			String[] result = nSpellChecker.suggestSimilar("samsung ssd", 2, SuggestMode.SUGGEST_MORE_POPULAR, 0.7f);
			Assert.assertEquals(SpellCheckResultSplit.split(result[0]), "samsung ssds");
			nSpellChecker.close();
		} finally {
			FileUtils.cleanUpDir(pathIndex2);
			FileUtils.cleanUpDir(pathTemp2);
			pathIndex2.delete();
			pathTemp2.delete();
		}
		
		ClusterStatusRequest clusterStatusRequest = new ClusterStatusRequest("http://localhost:" + ConfigurationManager.getSpellCheckConfig().getServicePort(), timeout);
		Response<ClusterStatus> clusterStatus = clusterStatusRequest.status();
		Assert.assertEquals(clusterStatus.getCode(), 200);
		Assert.assertTrue(clusterStatus.getBody().getGlobalIndexVersion() == 1L);
		Assert.assertTrue(clusterStatus.getBody().getInstanceStatus().size() == 1);
		InstanceStatus instanceStatus = clusterStatus.getBody().getInstanceStatus().get(0);
		Assert.assertTrue(instanceStatus.getCurrentVersion() == 1);
		Assert.assertTrue(instanceStatus.getAvailableVersions().size() == 2);
		Assert.assertTrue(instanceStatus.getAvailableVersions().contains(1L));
		Assert.assertTrue(instanceStatus.getAvailableVersions().contains(2L));
		
		DeleteRequest deleteRequest = new DeleteRequest("http://localhost:" + ConfigurationManager.getSpellCheckConfig().getServicePort(), timeout);
		Response<String> deleteResponse = deleteRequest.delete(1L, true);
		Assert.assertEquals(deleteResponse.getCode(), 500);
		deleteResponse = deleteRequest.delete(2L, false);
		Assert.assertEquals(deleteResponse.getCode(), 200);
		deleteResponse = deleteRequest.delete(2L, true);
		Assert.assertEquals(deleteResponse.getCode(), 200);
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
