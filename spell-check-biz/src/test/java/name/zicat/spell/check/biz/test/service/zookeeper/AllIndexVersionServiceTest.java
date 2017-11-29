package name.zicat.spell.check.biz.test.service.zookeeper;

import java.util.List;
import java.util.Map;

import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.conf.spellcheck.ZookeeperConfig;
import name.zicat.spell.check.biz.service.zookeeper.AllIndexVersionService;
import name.zicat.spell.check.biz.service.zookeeper.InstancesByIndexVersionService;
import name.zicat.spell.check.biz.service.zookeeper.register.IndexRegisterService;
import org.junit.Test;

import name.zicat.spell.check.client.model.IndexInfo;
import name.zicat.spell.check.client.model.ServiceInstance;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;
import name.zicat.spell.check.client.test.dao.zookeeper.ZookeeperServer;

import junit.framework.Assert;

/**
 * zicat
 */
public class AllIndexVersionServiceTest extends ZookeeperServer {
	
	@Test
	public void testIndexVersions() throws Exception {
		
		ZookeeperConfig zookeeperConfig = ConfigurationManager.getSpellCheckConfig().getZookeeperConfig();
		String rootPath = ConfigurationManager.getSpellCheckConfig().getZookeeperConfig().getRootPath();
		
		zookeeperConfig.setHostPort(getHostPort());
		
		ServiceInstance serviceInstance1 = new ServiceInstance("10.16.40.54", 8800);
		ServiceInstance serviceInstance2 = new ServiceInstance("10.16.40.54", 8810);
		IndexRegisterService indexRegisterService = new IndexRegisterService();
		indexRegisterService.register(new IndexInfo(serviceInstance1, 1L), null);
		indexRegisterService.register(new IndexInfo(serviceInstance1, 3L), null);
		
		ZookeeperClient zkClient = new CuratorZookeeperClient(zookeeperConfig.getHostPort(), zookeeperConfig.getSoTimeout());
		indexRegisterService = new IndexRegisterService(zkClient, rootPath);
		indexRegisterService.register(new IndexInfo(serviceInstance2, 1L), null);
		indexRegisterService.register(new IndexInfo(serviceInstance2, 2L), null);
		
		AllIndexVersionService service = new AllIndexVersionService();
		Map<Long, List<String>> indexVersions = service.getIndexVersions();
		Assert.assertEquals(indexVersions.size(), 3);
		Assert.assertEquals(indexVersions.get(1L).size(), 2);
		Assert.assertEquals(indexVersions.get(2L).size(), 1);
		Assert.assertEquals(indexVersions.get(3L).size(), 1);
		Assert.assertTrue(indexVersions.get(1L).contains("10.16.40.54:8800"));
		Assert.assertTrue(indexVersions.get(1L).contains("10.16.40.54:8810"));
		Assert.assertNull(indexVersions.get(4L));
		
		InstancesByIndexVersionService indexVersionService = new InstancesByIndexVersionService();
		Assert.assertNull(indexVersionService.getInstanceByIndexVersion(4L));
		
		Assert.assertTrue(indexVersionService.getInstanceByIndexVersion(1L).contains("10.16.40.54:8810"));
		Assert.assertTrue(indexVersionService.getInstanceByIndexVersion(1L).contains("10.16.40.54:8800"));
		
		indexVersionService = new InstancesByIndexVersionService(zkClient, rootPath);
		Assert.assertEquals(indexVersionService.getInstanceByIndexVersion(1L).size(), 2);
		Assert.assertEquals(indexVersionService.getInstanceByIndexVersion(2L).size(), 1);
		Assert.assertEquals(indexVersionService.getInstanceByIndexVersion(3L).size(), 1);
	}
}
