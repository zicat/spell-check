package name.zicat.spell.check.biz.rest;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import name.zicat.spell.check.biz.service.http.*;
import name.zicat.spell.check.client.model.Response;
import name.zicat.spell.check.client.model.SpellCheckModel;

/**
 * @author zicat
 */
@Path("/v1")
public class SpellCheckerController {

    /**
     * 
     * @param version
     * @return
     * @throws Exception 
     */
    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response download(@QueryParam("id") Long version) throws Exception {
    	DownloadService service = new DownloadService(version);
    	InputStream in = service.download();
    	return javax.ws.rs.core.Response.ok(in, MediaType.APPLICATION_OCTET_STREAM).build();
    }
    
    /**
     * 
     * @param version
     * @return
     */
    @GET
    @Path("/swap")
    @Produces(MediaType.APPLICATION_JSON)
    public Response<String> swap(@QueryParam("id") Long version) {
    	SwapService service = new SwapService(version);
    	return service.swap();
    }
    
    /**
     * 
     * @param keyword
     * @param row
     * @param version
     * @return
     */
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response<SpellCheckModel> spellCheck(@QueryParam("keyword") String keyword, @QueryParam("row") int row, @QueryParam("id") Long version) {
    	SpellCheckerService service = new SpellCheckerService(keyword, row, version);
    	return service.spellCheck();
    }
    
    /**
     * 
     * @param version
     * @param in
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces("application/json")
	@Path("/upload")
    public Response<String> upload(@QueryParam("id") Long version, InputStream in) {
    	UploadService service = new UploadService(version);
    	return service.upload(in);
    }
    
    @GET
	@Path("/delete")
    @Produces("application/json")
    public Response<String> delete(@QueryParam("id") Long version, @QueryParam("broadcast")@DefaultValue("true") boolean broadcast) {
    	DeleteService deleteService = new DeleteService(version, broadcast);
    	return deleteService.delete();
    }

    @GET
    @Path("/ping")
    @Produces("application/json")
    public Response<String> ping() {
        PingService pingService = new PingService();
        return pingService.ping();
    }

    @GET
    @Path("/pingTurnOn")
    @Produces("application/json")
    public Response<String> pingTurnOn() {
        PingService pingService = new PingService();
        return pingService.turnOn();
    }

    @GET
    @Path("/pingTurnOff")
    @Produces("application/json")
    public Response<String> pingTurnOff() {
        PingService pingService = new PingService();
        return pingService.turnOff();
    }
}
