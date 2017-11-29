package name.zicat.spell.check.biz.service.http;

import java.io.File;
import java.util.List;
import java.util.Map;

import name.zicat.spell.check.biz.utils.ZipUtil;
import name.zicat.spell.check.core.NSpellChecker;
import name.zicat.utils.file.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.zicat.spell.check.biz.cache.ZookeeperLocalCache;
import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.service.zookeeper.AvailableInstanceService;
import name.zicat.spell.check.biz.service.zookeeper.GlobalIndexVersionService;
import name.zicat.spell.check.client.model.Response;
import name.zicat.spell.check.client.model.ServiceInstance;
import name.zicat.spell.check.client.query.DeleteRequest;
import name.zicat.spell.check.client.utils.Constants;

/**
 * 
 * @author zicat
 *
 */
public class DeleteService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DeleteService.class);
	@SuppressWarnings("restriction")
	private static final String lineSeparator = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("line.separator"));
	
	private Long version;
	private boolean broadcast;
	
	public DeleteService(Long version, boolean broadcast) {
		
		this.version = version;
		this.broadcast = broadcast;
	}
	
	/**
	 * 
	 * @return
	 */
	public Response<String> delete() {
		
		Response<String> response = new Response<>();
		try {
			
			ServiceInstance currentInstance = ConfigurationManager.getSpellCheckConfig().create();
			Map.Entry<Long, NSpellChecker> entry = ZookeeperLocalCache.getCache().getCurrentSpellCheckerEntry();
			// check local whether used
			if(entry != null && entry.getKey() == version) {
				response.setCode(500);
				response.setMessage("Version is using now, Please swap first!Instance Id " + currentInstance);
			} else {
				GlobalIndexVersionService globalIndexVersionService = new GlobalIndexVersionService();
				Long globalIndexVersion = globalIndexVersionService.getGlobalIndexVersion();
				// check global whether used
				if(globalIndexVersion != null && globalIndexVersion.longValue() == version.longValue()) {
					response.setCode(500);
					response.setMessage("Version is using now, Please swap first!Instance Id " + currentInstance);
				} else {
					NSpellChecker nSpellChecker = ZookeeperLocalCache.getCache().removeSpellChecker(version);
					if(nSpellChecker != null) {
						nSpellChecker.close();
						String indexPath = ConfigurationManager.getSpellCheckConfig().getIndexConfig().getIndexPath();
						File indexDir = new File(indexPath, String.valueOf(version));
						if(indexDir.exists()) {
							FileUtils.cleanUpDir(indexDir);
							indexDir.delete();
						}
						
						File indexZip = new File(indexPath, String.valueOf(version + ZipUtil.SUFFIX_ZIP));
						if(indexZip.exists()) {
							indexZip.delete();
						}
					}
				}
				
			}
			
			if(broadcast) {
				AvailableInstanceService availableInstanceService = new AvailableInstanceService();
				List<String> instances = availableInstanceService.getInstance();
				for(String instance: instances) {
					
					if(currentInstance.toString().equals(instance))
						continue;
					
					try {
						DeleteRequest request = new DeleteRequest(Constants.HTTP_PREFIX + instance);
						Response<String> otherInstanceResponse = request.delete(version, false);
						if(otherInstanceResponse.getCode() != 200) {
							response.setCode(otherInstanceResponse.getCode());
							if(response.getMessage()== null) {
								response.setMessage(otherInstanceResponse.getMessage());
							} else {
								response.setMessage(response.getMessage() + lineSeparator + otherInstanceResponse.getMessage());
							}
						}
					} catch(Exception e) {
						LOG.warn("Broadcast Delete error, ignore and continue delete", e);
					}
				}
			}
		} catch(Exception e) {
			response.setCode(500);
			response.setMessage(e.toString());
			LOG.error("delete service exception", e);
		}
		return response;
	}
}