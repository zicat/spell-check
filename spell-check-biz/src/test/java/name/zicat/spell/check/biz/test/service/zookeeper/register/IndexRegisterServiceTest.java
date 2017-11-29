package name.zicat.spell.check.biz.test.service.zookeeper.register;

import name.zicat.spell.check.biz.service.zookeeper.register.IndexRegisterService;
import org.junit.Test;

import name.zicat.spell.check.client.model.IndexInfo;
import name.zicat.spell.check.client.model.ServiceInstance;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.test.dao.zookeeper.ZookeeperServer;

import junit.framework.Assert;

/**
 * 
 * @author zicat
 *
 */
public class IndexRegisterServiceTest extends ZookeeperServer {
	
	@Test
	public void test() throws Exception {
		
		CuratorZookeeperClient zkClient = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		IndexRegisterService service = new IndexRegisterService(zkClient, "/spellcheck");
		ServiceInstance serviceInstance = new ServiceInstance("localhost", 7788);
		
		IndexInfo t = new IndexInfo(serviceInstance, 1L);
		Assert.assertFalse(service.isRegistered(t));
		service.register(t, null);
		
		t = new IndexInfo(serviceInstance, 2L);
		Assert.assertFalse(service.isRegistered(t));
		service.register(t, null);
		
		
		Assert.assertTrue(service.isRegistered(t));
		
		try {
			service.register(t, null);
			Assert.assertTrue(false);
		} catch(Throwable e) {
			Assert.assertTrue(true);
		}
		t.setVersion(1);
		Assert.assertTrue(service.isRegistered(t));
		String path = service.buildPath(t);
		Assert.assertEquals(path, "/spellcheck/index/1/localhost:7788");
	}
}
