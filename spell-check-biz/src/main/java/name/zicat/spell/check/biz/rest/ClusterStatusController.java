package name.zicat.spell.check.biz.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import name.zicat.spell.check.biz.service.cluster.ClusterStatusService;
import name.zicat.spell.check.client.model.ClusterStatus;
import name.zicat.spell.check.client.model.Response;

/**
 * @author zicat
 */
@Path("/v1")
public class ClusterStatusController {

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	@GET
    @Path("/cluster/status")
    @Produces(MediaType.APPLICATION_JSON)
	public Response<ClusterStatus> zkStatus() throws Exception {
		ClusterStatusService service = new ClusterStatusService();
		return service.status();
	}
}
