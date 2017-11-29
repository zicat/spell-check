package name.zicat.spell.check.biz.service.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.zicat.spell.check.biz.service.http.SpellCheckerService;
import name.zicat.spell.check.biz.service.zookeeper.AllIndexVersionService;
import name.zicat.spell.check.biz.service.zookeeper.AvailableInstanceService;
import name.zicat.spell.check.biz.service.zookeeper.GlobalIndexVersionService;
import name.zicat.spell.check.biz.service.zookeeper.InstanceCurrentVersionService;
import name.zicat.spell.check.client.model.ClusterStatus;
import name.zicat.spell.check.client.model.InstanceStatus;
import name.zicat.spell.check.client.model.Response;
import name.zicat.spell.check.client.utils.Constants;

/**
 * @author zicat
 */
public class ClusterStatusService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SpellCheckerService.class);

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public Response<ClusterStatus> status() throws Exception {
		
		Response<ClusterStatus> response = new Response<>();
		try {
			GlobalIndexVersionService gloablIndexVersionService = new GlobalIndexVersionService();
			Long globalIndexVersion = gloablIndexVersionService.getGlobalIndexVersion();
			
			AllIndexVersionService allIndexVersionService = new AllIndexVersionService();
			Map<Long, List<String>> versions = allIndexVersionService.getIndexVersions();
			Map<String, List<Long>> instanceVersions = convert(versions);
			
			InstanceCurrentVersionService instanceCurrentVersionService = new InstanceCurrentVersionService();
			Map<String, Long> currentVersionMap = instanceCurrentVersionService.getInstanceCurrentVersion();
			
			AvailableInstanceService availableInstanceService = new AvailableInstanceService();
			List<String> instances = availableInstanceService.getInstance();
			
			ClusterStatus clusterStatus = new ClusterStatus();
			clusterStatus.setGlobalIndexVersion(globalIndexVersion);
			response.setBody(clusterStatus);
			if(instances != null) {
				List<InstanceStatus> instanceStatus = new ArrayList<>();
				for(String instance: instances) {
					InstanceStatus status = new InstanceStatus();
					status.setInstanceId(instance);
					if(instanceVersions != null)
						status.setAvailableVersions(instanceVersions.get(instance));
					if(currentVersionMap != null)
						status.setCurrentVersion(currentVersionMap.get(instance));
					instanceStatus.add(status);
				}
				clusterStatus.setInstanceStatus(instanceStatus);
			}
		} catch(Exception e) {
			response.setCode(Constants.HTTP_STATUS_ERROR);
			response.setMessage(e.toString());
			LOG.error("cluster status service exception", e);
		}
		return response;
	}

	/**
	 *
	 * @param versions
	 * @return
	 */
	private Map<String, List<Long>> convert(Map<Long, List<String>> versions) {
		
		if(versions == null) {
			return null;
		}
		
		Map<String, List<Long>> result = new HashMap<>();
		for(Entry<Long, List<String>> entry: versions.entrySet()) {
			
			if(entry.getValue() == null) 
				continue;
			
			for(String instance: entry.getValue()) {
				if(instance == null)
					continue;
				List<Long> values = result.get(instance);
				if(values == null) {
					values = new ArrayList<>();
					result.put(instance, values);
				}
				values.add(entry.getKey());
			}
		}
		return result;
	}
}
