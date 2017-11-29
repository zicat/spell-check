package name.zicat.spell.check.client.query;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;

import name.zicat.spell.check.client.dao.http.HttpClient;
import name.zicat.spell.check.client.dao.http.IHttpClient;
import name.zicat.spell.check.client.model.Response;
import org.apache.http.entity.InputStreamEntity;

import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author zicat
 *
 */
public class UploadRequest extends HttpRequest {
	
	public UploadRequest(String url, String version, int timeOut) {
		super(url, version, timeOut);
	}
	
	public UploadRequest(String url, int timeOut) {
		super(url, timeOut);
	}
	
	public UploadRequest(String url) {
		super(url);
	}
	
	/**
	 * upload dictionary
	 * @param id
	 * @param body
	 * @throws IOException 
	 */
	public Response<String> upload(Long id, InputStream body) throws IOException {
		
		IHttpClient client = new HttpClient(url, timeOut);
		String path = buildPath(UPLOAD_PATH, version);
		List<Entry<String, String>> params = getIdParam(id);
		String response = client.post(path, params, null, new InputStreamEntity(body), StandardCharsets.UTF_8);
		Type jsonType = new TypeToken<Response<String>>() {}.getType(); 
		return GSON.fromJson(response, jsonType);
	}
}
