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

/**
 * 
 * @author lz31
 *
 */
public class SwapRequest extends HttpRequest {

	public SwapRequest(String url, int timeOut) {
		super(url, timeOut);
	}
	
	public SwapRequest(String url, String version, int timeOut) {
		super(url, version, timeOut);
	}
	
	public SwapRequest(String url) {
		super(url);
	}

	/**
	 *
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public Response<String> swap(Long id) throws IOException {
		
		IHttpClient client = new HttpClient(url, timeOut);
		String path = buildPath(SWAP_PATH, version);
		List<Entry<String, String>> params = getIdParam(id);
		String values = client.get(path, params, null, StandardCharsets.UTF_8);
		Type jsonType = new TypeToken<Response<String>>() {}.getType();  
		return GSON.fromJson(values, jsonType);
	}
}
