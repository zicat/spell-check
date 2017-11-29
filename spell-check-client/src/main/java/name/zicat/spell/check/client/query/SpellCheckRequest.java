package name.zicat.spell.check.client.query;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.reflect.TypeToken;
import name.zicat.spell.check.client.dao.http.HttpClient;
import name.zicat.spell.check.client.dao.http.IHttpClient;
import name.zicat.spell.check.client.model.Response;
import name.zicat.spell.check.client.model.SpellCheckModel;
import name.zicat.utils.ds.map.CommonEntry;

/**
 * @author zicat
 */
public class SpellCheckRequest extends HttpRequest {
	
	public SpellCheckRequest(String url, int timeOut) {
		super(url, timeOut);
	}
	
	public SpellCheckRequest(String url, String version, int timeOut) {
		super(url, version, timeOut);
	}
	
	public SpellCheckRequest(String url) {
		super(url);
	}

	/**
	 *
	 * @param keyword
	 * @param row
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public Response<SpellCheckModel> spellCheck(String keyword, int row, Long id) throws IOException {
		
		if(keyword == null)
			return null;
		
		IHttpClient client = new HttpClient(url, timeOut);
		String path = buildPath(SPELL_CHECK_PATH, version);;
		
		List<Entry<String, String>> params = new ArrayList<>();
		params.add(new CommonEntry<>(KEYWORD_PARAM, keyword));
		
		if(row > 0)
			params.add(new CommonEntry<>(ROW_PARAM, String.valueOf(row)));
		
		if(id != null)
			params.add(new CommonEntry<>(ID_PARAM, String.valueOf(id)));
		
		String value = client.get(path, params, null, StandardCharsets.UTF_8);
		Type jsonType = new TypeToken<Response<SpellCheckModel>>() {}.getType();  
		Response<SpellCheckModel> response = GSON.fromJson(value, jsonType);
		return response;
	}
}
