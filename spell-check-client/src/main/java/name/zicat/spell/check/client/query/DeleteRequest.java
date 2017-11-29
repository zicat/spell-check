package name.zicat.spell.check.client.query;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.reflect.TypeToken;
import name.zicat.spell.check.client.dao.http.HttpClient;
import name.zicat.spell.check.client.dao.http.IHttpClient;
import name.zicat.spell.check.client.model.Response;
import name.zicat.utils.ds.map.CommonEntry;

/**
 * 
 * @author zicat
 *
 */
public class DeleteRequest extends HttpRequest {

	public DeleteRequest(String url, int timeOut) {
		super(url, timeOut);
	}
	
	public DeleteRequest(String url, String version, int timeOut) {
		super(url, version, timeOut);
	}
	
	public DeleteRequest(String url) {
		super(url);
	}
	
	/**
	 * 
	 * @param id
	 * @param broadcast
	 * @return
	 * @throws IOException
	 */
	public Response<String> delete(Long id, boolean broadcast) throws IOException {
		
		IHttpClient client = new HttpClient(url, timeOut);
		String path = buildPath(DELETE_PATH, version);
		List<Entry<String, String>> params = getIdParam(id);
		params.add(new CommonEntry<>("broadcast", String.valueOf(broadcast)));
		String values = client.get(path, params, null, StandardCharsets.UTF_8);
		Type jsonType = new TypeToken<Response<String>>() {}.getType();  
		return GSON.fromJson(values, jsonType);
	}
}
