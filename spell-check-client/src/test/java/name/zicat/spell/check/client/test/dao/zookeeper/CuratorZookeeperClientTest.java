package name.zicat.spell.check.client.test.dao.zookeeper;

import com.google.common.primitives.Longs;
import name.zicat.spell.check.client.dao.zookeeper.CuratorZookeeperClient;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient.WatcherHandler;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Test;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zicat
 * @date 2017/06/21
 */
public class CuratorZookeeperClientTest extends ZookeeperServer {
	
	@Test
	public void test() throws Exception {
		
		testCreateAndDeleteNode();
		testSetAndGetData();
		testEphemeralNode();
		testWatcher();
		testConstruct();
	}
	
	public void testConstruct() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		CuratorZookeeperClient zkManager = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		CuratorZookeeperClient zkManager2 = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		
		Field clientField = CuratorZookeeperClient.class.getDeclaredField("client");
		clientField.setAccessible(true);
		CuratorFramework client1 = (CuratorFramework) clientField.get(zkManager);
		CuratorFramework client2 = (CuratorFramework) clientField.get(zkManager2);
		Assert.assertTrue(client1 == client2);
	}
	
	public void testCreateAndDeleteNode() throws Exception {
		
		CuratorZookeeperClient zkManager = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		if (!zkManager.checkExists("/test2/create4test")) {
			zkManager.createNode("/test2/create4test", null);
			zkManager.createNode("/test2/create5test", null);
		}
		Assert.assertTrue(zkManager.checkExists("/test2/create4test"));
		Assert.assertTrue(zkManager.checkExists("/test2/create5test"));
		List<String>  child = zkManager.listChildrenNode("/test2");
		Assert.assertTrue(child.contains("create4test"));
		Assert.assertTrue(child.contains("create5test"));
		Assert.assertEquals(child.size(), 2);
		zkManager.deleteNode("/test2/create4test");
		Assert.assertFalse(zkManager.checkExists("/test2/create4test"));
	}

	public void testSetAndGetData() throws Exception {
		
		CuratorZookeeperClient zkManager = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		if (!zkManager.checkExists("/test/create4test")) {
			zkManager.createNode("/test/create4test", null);
		}
		Assert.assertTrue(zkManager.checkExists("/test/create4test"));

		zkManager.setData("/test/create4test", "create4test".getBytes());
		Assert.assertEquals("create4test", new String(zkManager.getData("/test/create4test")));

		zkManager.deleteNode("/test/create4test");
		Assert.assertFalse(zkManager.checkExists("/test/create4test"));
	}

	public void testEphemeralNode() throws Exception {
		
		CuratorZookeeperClient zkManager = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		if (zkManager.checkExists("/test3/ephemeralNode")) {
			zkManager.deleteNode("/test3/ephemeralNode");
		}
		Closeable node = zkManager.loginEphemeralNode("/test3/ephemeralNode/node", "test ephemeral".getBytes());
		Assert.assertTrue(zkManager.checkExists("/test3/ephemeralNode/node"));
		Assert.assertTrue("test ephemeral".equals(new String(zkManager.getData("/test3/ephemeralNode/node"))));

		CuratorZookeeperClient zkManager2 = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		Assert.assertTrue(zkManager2.checkExists("/test3/ephemeralNode/node"));
		zkManager.logoutEphemeralNode(node);
		Assert.assertFalse(zkManager.checkExists("/test3/ephemeralNode/node"));
	}

	public void testWatcher() throws Exception {
		
		CuratorZookeeperClient zkManager = new CuratorZookeeperClient(getHostPort(), 15000, 15000);
		if (!zkManager.checkExists("/test4/watcher")) {
			zkManager.createNode("/test4/watcher", null);
		}
		
		ReentrantLock lock = new ReentrantLock();
		Condition changedCondition = lock.newCondition();
		
		final AtomicInteger changed = new AtomicInteger(0);
		
		zkManager.addNodeDataChangedWatcher("/test4/watcher", new WatcherHandler() {
			
			@Override
			public void process(String path) {
				lock.lock();
				try {
					System.out.println("processing");
					changed.incrementAndGet();
					Assert.assertEquals(path, "/test4/watcher");
					changedCondition.signal();
				} finally {
					lock.unlock();
				}
			}
		});
		
		lock.lock();
		try {
			zkManager.setData("/test4/watcher", Longs.toByteArray(123456L));
			changedCondition.await(3, TimeUnit.SECONDS);
		} finally {
			lock.unlock();
		}
		Assert.assertTrue(changed.get() == 1);
		
		Thread.sleep(1000); // wait for regist
		lock.lock();
		try {
			zkManager.setData("/test4/watcher", Longs.toByteArray(123456L));
			changedCondition.await(3, TimeUnit.SECONDS);
		} finally {
			lock.unlock();
		}
		Assert.assertTrue(changed.get() == 2);
	}
}
