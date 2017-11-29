package name.zicat.spell.check.biz.test.service.zookeeper.register;

import java.nio.charset.StandardCharsets;

import name.zicat.spell.check.biz.service.zookeeper.register.CurrentIndexRegisterService;
import org.junit.Test;

import name.zicat.spell.check.client.model.ServiceInstance;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.test.dao.zookeeper.ZookeeperServer;

import junit.framework.Assert;

/**
 * 
 * @author zicat
 *
 */
public class CurrentIndexRegisterServiceTest extends ZookeeperServer {
	
	@Test
	public void test() throws Exception {
		
		CuratorZookeeperClient zkClient = new CuratorZookeeperClient(getHostPort(), 15000);
		CurrentIndexRegisterService service = new  CurrentIndexRegisterService(zkClient, "/spellcheck");
		ServiceInstance serviceInstance = new ServiceInstance("localhost", 7788);
		
		Assert.assertFalse(service.isRegistered(serviceInstance));
		service.register(serviceInstance, String.valueOf(1L).getBytes(StandardCharsets.UTF_8));
		Assert.assertTrue(service.isRegistered(serviceInstance));
		try {
			service.register(serviceInstance, String.valueOf(1L).getBytes(StandardCharsets.UTF_8));
			Assert.assertTrue(true);
		} catch(Throwable e) {
			Assert.assertTrue(false);
		}
		String path = service.buildPath(serviceInstance);
		Assert.assertEquals(path, "/spellcheck/node_version_localhost:7788");
		String data = new String(zkClient.getData(path), StandardCharsets.UTF_8);
		Assert.assertEquals(data, "1");
		
		service.register(serviceInstance, String.valueOf(2L).getBytes(StandardCharsets.UTF_8));
		Assert.assertTrue(service.isRegistered(serviceInstance));
		path = service.buildPath(serviceInstance);
		Assert.assertEquals(path, "/spellcheck/node_version_localhost:7788");
		data = new String(zkClient.getData(path), StandardCharsets.UTF_8);
		Assert.assertEquals(data, "2");
	}
}
