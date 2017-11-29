package name.zicat.spell.check.biz.service.zookeeper.register;

import name.zicat.spell.check.client.model.IndexInfo;
import name.zicat.spell.check.client.dao.zookeeper.ZookeeperClient;
import name.zicat.spell.check.client.utils.Constants;

/**
 * @author zicat
 * @date 2017/06/24
 */
public class IndexRegisterService extends AbstractRegisterService<IndexInfo> {

	public static final String INDEX_PATH = "index";

	public IndexRegisterService(ZookeeperClient zkClient, String rootPath) {

		super(zkClient, rootPath);
	}
	
	public IndexRegisterService() throws Exception {
		super();
	}
	
	/**
	 * Path Struct: ${spell_check_root}/index/${index_id}/${ip}:${port} Example
	 * : /spellcheck/index/1/localhost:8081
	 */
	@Override
	public String buildPath(IndexInfo t) {

		return getIndexRootPath(rootPath) + Constants.SLASH + t.getVersion() + Constants.SLASH
				+ t.getServiceInstance().getId();
	}

	/**
	 *
	 * @param rootPath
	 * @return
	 */
	public static String getIndexRootPath(String rootPath) {
		
		return rootPath + Constants.SLASH + INDEX_PATH;
	}
}
