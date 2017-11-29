package name.zicat.spell.check.biz.service.zookeeper.register;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import name.zicat.spell.check.biz.service.zookeeper.AbstractZookeeperService;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;

/**
 * 
 * @author zicat
 *
 * @param <T>
 */
public abstract class AbstractRegisterService<T> extends AbstractZookeeperService implements RegisterService<T> {
	
	private static final Map<String, Closeable> registerMap = new ConcurrentHashMap<>();
	
	public AbstractRegisterService(ZookeeperClient zkClient, String rootPath) {
		
		super(zkClient, rootPath);
	}
	
	public AbstractRegisterService() throws Exception {
		super();
	}

	/**
	 *
	 * @param t
	 * @throws Exception
	 */
	@Override
	public void register(T t, byte[] data) throws Exception {
		
		String path = buildPath(t);
    	if(isRegistered(t))
    		throw new Exception(path + " has registered! Registered Fail");
    	
    	Closeable closeable;
    	if(data == null) 
    		closeable = zkClient.loginEphemeralNode(path, String.valueOf(new Date()).getBytes(StandardCharsets.UTF_8));
    	else
    		closeable = zkClient.loginEphemeralNode(path, data);
    	registerMap.put(path, closeable);
	}
	
	@Override
	public void cancell(T t) throws Exception {
		
		String path = buildPath(t);
		Closeable closeable = registerMap.remove(path);
		if(closeable != null)
			zkClient.logoutEphemeralNode(closeable);;
	}
	 

	/**
	 *
	 * @param t
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean isRegistered(T t) throws Exception {
		
		 return zkClient.checkExists(buildPath(t));
	}

	/**
	 *
	 * @param t
	 * @return
	 */
	protected abstract String buildPath(T t);
}
