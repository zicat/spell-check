package name.zicat.spell.check.client.query;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import com.google.gson.reflect.TypeToken;
import name.zicat.spell.check.client.dao.http.HttpClient;
import name.zicat.spell.check.client.dao.http.IHttpClient;
import name.zicat.spell.check.client.model.ClusterStatus;
import name.zicat.spell.check.client.model.Response;

/**
 * 
 * @author zicat
 *
 */
public class ClusterStatusRequest extends HttpRequest {

	public ClusterStatusRequest(String url, String version, int timeOut) {
		super(url, version, timeOut);
	}
	
	public ClusterStatusRequest(String url, int timeOut) {
		super(url, timeOut);
	}
	
	public ClusterStatusRequest(String url) {
		super(url);
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public Response<ClusterStatus> status() throws IOException {
		
		IHttpClient client = new HttpClient(url, timeOut);
		String path = buildPath(CLUSTER_STATUS_PATH, version);
		String values = client.get(path, null, null, StandardCharsets.UTF_8);
		Type jsonType = new TypeToken<Response<ClusterStatus>>() {}.getType();  
		return GSON.fromJson(values, jsonType);
	}
}	

