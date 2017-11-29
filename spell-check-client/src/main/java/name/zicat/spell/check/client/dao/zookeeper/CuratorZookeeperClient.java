package name.zicat.spell.check.client.dao.zookeeper;

import name.zicat.utils.io.IOUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.PathUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.Watcher;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author zicat
 */
public class CuratorZookeeperClient implements ZookeeperClient {

	private static final Logger LOG = Logger.getLogger(CuratorZookeeperClient.class);
	
	private CuratorFramework client;
	private static final Map<String, CuratorFramework> zkManager = new ConcurrentHashMap<>();
	
	public CuratorZookeeperClient(String hostPort, int sessionTimeout) {
		this(hostPort, sessionTimeout / 2, sessionTimeout);
	}

	/**
	 *
	 * @param hostPort
	 * @param connectionTimeout
	 * @param sessionTimeout
	 */
	public CuratorZookeeperClient(String hostPort, int connectionTimeout, int sessionTimeout) {
		
		if(zkManager.containsKey(hostPort))
			client = zkManager.get(hostPort);
		
		synchronized (zkManager) {
			if(!zkManager.containsKey(hostPort)) {
				client = CuratorFrameworkFactory.builder().connectString(hostPort)
						.retryPolicy(new ExponentialBackoffRetry(1000, 3)).connectionTimeoutMs(connectionTimeout)
						.sessionTimeoutMs(sessionTimeout).build();
				client.start();
				zkManager.put(hostPort, client);
			}
		}
		client = zkManager.get(hostPort);
	}

	/**
	 *
	 * @param hostPort
	 */
	public synchronized static void shutDown(String hostPort) {
		
		CuratorFramework client = zkManager.remove(hostPort);
		IOUtils.closeQuietly(client);
	}

	/**
	 *
	 */
	public synchronized static void shutDownAll() {
		
		if(!zkManager.isEmpty()) {
			for(Entry<String, CuratorFramework> entry: zkManager.entrySet()) {
				IOUtils.closeQuietly(entry.getValue());
			}
			zkManager.clear();
		}
	}

	@Override
	public boolean createNode(String path, byte[] value) throws Exception {
		String result = null;
		PathUtils.validatePath(path);
		result = client.create().creatingParentsIfNeeded().forPath(path, value);
		LOG.info("create node:" + path);
		return result != null;
	}

	@Override
	public boolean deleteNode(String path) throws Exception {
		
		PathUtils.validatePath(path);
		client.delete().deletingChildrenIfNeeded().forPath(path);
		LOG.info("delete node:" + path);
		return true;
	}

	@Override
	public void setData(String path, byte[] content) throws Exception {
		
		PathUtils.validatePath(path);
		client.setData().forPath(path, content);
	}

	@Override
	public byte[] getData(String path) throws Exception {
		
		PathUtils.validatePath(path);
		return client.getData().forPath(path);
	}

	@Override
	public List<String> listChildrenNode(String path) throws Exception {
		
		PathUtils.validatePath(path);
		return client.getChildren().forPath(path);
	}

	@Override
	public boolean checkExists(String path) throws Exception {
		
		PathUtils.validatePath(path);
		return client.checkExists().forPath(path) != null;
	}

	@Override
	public void logoutEphemeralNode(Closeable node) throws Exception {
		if (node != null)
			node.close();
	}

	@Override
	public Closeable loginEphemeralNode(String path, byte[] data) throws Exception {
		PersistentEphemeralNode persistentEphemeralNode = new PersistentEphemeralNode(client,
				PersistentEphemeralNode.Mode.EPHEMERAL, path, data);
		persistentEphemeralNode.start();
		persistentEphemeralNode.waitForInitialCreate(3000, TimeUnit.MILLISECONDS);
		return persistentEphemeralNode;
	}

	@Override
	public void addNodeDataChangedWatcher(final String path, final WatcherHandler wathcherHandler) throws Exception {
		
		if(!checkExists(path))
			createNode(path, null);
		
		CuratorZookeeperClient thisClient = this;
		Watcher watcher = event -> {
            try {
            	if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
            		wathcherHandler.process(path);
            	}
            } finally {
            	try {
            		thisClient.addNodeDataChangedWatcher(path, wathcherHandler);
            	} catch (Exception e) {
            		LOG.error("Readd Change Error", e);
            	}
            }
        };
        client.getData().usingWatcher(watcher).inBackground().forPath(path);
	}
}
