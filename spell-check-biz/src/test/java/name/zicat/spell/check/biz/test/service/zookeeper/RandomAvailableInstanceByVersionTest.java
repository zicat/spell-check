package name.zicat.spell.check.biz.test.service.zookeeper;

import org.junit.Assert;
import org.junit.Test;

import name.zicat.spell.check.biz.service.zookeeper.RandomAvailableInstanceByVersion;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.test.dao.zookeeper.ZookeeperServer;

public class RandomAvailableInstanceByVersionTest extends ZookeeperServer {
	
	@Test
	public void test() throws Exception {
		
		CuratorZookeeperClient zkClient = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		
		RandomAvailableInstanceByVersion service = new RandomAvailableInstanceByVersion(zkClient, "/spellcheck");
		try {
			service.random(1L);
			Assert.assertTrue(false);
		} catch(Throwable e) {
			Assert.assertTrue(true);
		}
	}
}
