package name.zicat.spell.check.biz.service.zookeeper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import name.zicat.spell.check.biz.service.zookeeper.register.IndexRegisterService;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;
import name.zicat.spell.check.client.utils.Constants;

/**
 * 
 * @author zicat
 *
 */
public class GlobalIndexVersionService extends AbstractZookeeperService {

	private static final String CURRENT_VERSION = "current_version";
	
	public GlobalIndexVersionService(ZookeeperClient zkClient, String rootPath) {
		super(zkClient, rootPath);
	}
	
	public GlobalIndexVersionService() throws Exception {
		super();
	}
	
	/**
	 * Glogbal Version Path: ${spell_check_root}/current_version
	 * 				   Data:String to Long
	 * @return
	 * @throws Exception
	 */
	public Long getGlobalIndexVersion() throws Exception {
		
		String path = getPath(rootPath);
		if(!zkClient.checkExists(path)) {
			return null;
		}
		
		byte[] bs = zkClient.getData(path);
		String version = new String(bs, StandardCharsets.UTF_8);
		return Long.valueOf(version);
	}
	
	/**
	 * 
	 * @param version
	 * @throws Exception
	 */
	public void setGlobalIndexVersion(long version) throws Exception {
		
		String vs = String.valueOf(version);
		
		String indexRootPath = IndexRegisterService.getIndexRootPath(rootPath);
		List<String> children = zkClient.listChildrenNode(indexRootPath);
		
		boolean containsVersion = false;
		if(children != null) {
			for(String child: children) {
				if(child.equals(vs)) {
					containsVersion = true;
					break;
				}
			}
		}
		
		if(!containsVersion) {
			throw new RuntimeException("version number error, please check avalible version in path " + indexRootPath);
		}
		
		String path = getPath(rootPath);
		if(!zkClient.checkExists(path)) {
			zkClient.createNode(path, vs.getBytes(StandardCharsets.UTF_8));
		} else {
			zkClient.setData(path, vs.getBytes(StandardCharsets.UTF_8));
		}
	}

	/**
	 *
	 * @return
	 */
	public static String getPath(String rootPath) {
		return rootPath + Constants.SLASH + CURRENT_VERSION;
	}
}
