package name.zicat.spell.check.biz.service.http;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.zicat.spell.check.biz.service.zookeeper.GlobalIndexVersionService;
import name.zicat.spell.check.biz.service.zookeeper.InstancesByIndexVersionService;
import name.zicat.spell.check.client.model.Response;
import name.zicat.spell.check.client.utils.Constants;

/**
 * 
 * @author lz31
 *
 */
public class SwapService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SwapService.class);
	
	private Long version;

	public SwapService(Long version) {
		this.version = version;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Response<String> swap() {

		Response<String> response = new Response<>();
		
		try {
			GlobalIndexVersionService service = new GlobalIndexVersionService();
			InstancesByIndexVersionService instancesByIndexVersionService = new InstancesByIndexVersionService();
			List<String> instances = instancesByIndexVersionService.getInstanceByIndexVersion(version);
			if (instances != null && !instances.isEmpty()) {
				service.setGlobalIndexVersion(version);
				response.setMessage("Swap Success");
				response.setBody(toString(instances));
			} else {
				response.setMessage("No Aviable Instance Contains Spell Check Version " + version);
				response.setCode(Constants.HTTP_STATUS_ERROR);
			}
			return response;
		} catch(Exception e) {
			response.setCode(Constants.HTTP_STATUS_ERROR);
			response.setMessage(e.toString());
			LOG.error("swap service exception, version = " + version, e);
		}
		return response;
	}
	
	/**
	 * 
	 * @param instances
	 * @return
	 */
	public String toString(List<String> instances) {

		StringBuilder sb = new StringBuilder();
		Iterator<String> it = instances.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(Constants.SEMICOLON);
			}
		}
		return sb.toString();
	}
}
