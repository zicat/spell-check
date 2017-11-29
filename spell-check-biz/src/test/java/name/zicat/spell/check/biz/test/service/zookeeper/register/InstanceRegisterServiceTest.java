package name.zicat.spell.check.biz.test.service.zookeeper.register;

import org.junit.Assert;
import org.junit.Test;

import name.zicat.spell.check.client.model.ServiceInstance;
import name.zicat.spell.check.biz.service.zookeeper.register.InstanceRegisterService;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.test.dao.zookeeper.ZookeeperServer;


/**
 * 
 * @author zicat
 *
 */
public class InstanceRegisterServiceTest extends ZookeeperServer {
	
	@Test
	public void test() throws Exception {
		
		CuratorZookeeperClient zkClient = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		InstanceRegisterService service = new InstanceRegisterService(zkClient, "/spellcheck");
		ServiceInstance serviceInstance = new ServiceInstance("localhost", 7788);
		
		Assert.assertTrue(!service.isRegistered(serviceInstance));
		service.register(serviceInstance, null);
		Assert.assertTrue(service.isRegistered(serviceInstance));
		
		String path = service.buildPath(serviceInstance);
		Assert.assertEquals(path, "/spellcheck/livenode/localhost:7788");
	}
}
