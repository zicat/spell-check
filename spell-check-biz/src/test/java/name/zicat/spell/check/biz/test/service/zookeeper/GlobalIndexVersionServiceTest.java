package name.zicat.spell.check.biz.test.service.zookeeper;

import org.junit.Test;

import name.zicat.spell.check.client.model.IndexInfo;
import name.zicat.spell.check.client.model.ServiceInstance;
import name.zicat.spell.check.biz.service.zookeeper.GlobalIndexVersionService;
import name.zicat.spell.check.biz.service.zookeeper.register.IndexRegisterService;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.test.dao.zookeeper.ZookeeperServer;

import junit.framework.Assert;

/**
 * 
 * @author lz31
 *
 */
public class GlobalIndexVersionServiceTest extends ZookeeperServer {
	
	@Test
	public void test() throws Exception {
		
		CuratorZookeeperClient zkClient = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		GlobalIndexVersionService service = new GlobalIndexVersionService(zkClient, "/spellcheck");
		zkClient.createNode(IndexRegisterService.getIndexRootPath("/spellcheck"), null);
		
		Assert.assertEquals(service.getGlobalIndexVersion(), null);
		
		Long version = System.currentTimeMillis();
		try {
			service.setGlobalIndexVersion(version);
			Assert.assertTrue(false);
		} catch(Throwable e) {
			Assert.assertTrue(true);
		}
		
		
		IndexRegisterService registerService = new IndexRegisterService(zkClient, "/spellcheck");
		IndexInfo index = new IndexInfo(new ServiceInstance("localhost", 8080), version);
		registerService.register(index, null);
		
		service.setGlobalIndexVersion(version);
		Long zookeeperVersion = service.getGlobalIndexVersion();
		Assert.assertEquals(version, zookeeperVersion);
		
		version = System.currentTimeMillis();
		index.setVersion(version);
		registerService.register(index, null);
		service.setGlobalIndexVersion(version);
		zookeeperVersion = service.getGlobalIndexVersion();
		Assert.assertEquals(version, zookeeperVersion);

		String path = GlobalIndexVersionService.getPath("/spellcheck");
		Assert.assertEquals(path, "/spellcheck/current_version");
		
		CuratorZookeeperClient.shutDownAll();
	}
}
