package name.zicat.spell.check.client.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;
import name.zicat.utils.ds.map.CommonEntry;

/**
 * 
 * @author zicat
 *
 */
public abstract class HttpRequest {
	
	public static final String UPLOAD_PATH = "/spellcheck/rest/%s/upload";
	public static final String DOWNLOAD_PATH = "/spellcheck/rest/%s/download";
	public static final String SWAP_PATH = "/spellcheck/rest/%s/swap";
	public static final String SPELL_CHECK_PATH = "/spellcheck/rest/%s/search";
	public static final String DELETE_PATH = "/spellcheck/rest/%s/delete";
	public static final String CLUSTER_STATUS_PATH = "/spellcheck/rest/%s/cluster/status";
	public static final String PING_PATH = "/spellcheck/rest/%s/ping";
	public static final String PING_TURN_ON_PATH = "/spellcheck/rest/%s/pingTurnOn";
	public static final String PING_TURN_OFF_PATH = "/spellcheck/rest/%s/pingTurnOff";
	
	public static final String DEFAULT_VERSION = "v1";
	public static final String ROW_PARAM = "row";
	public static final String KEYWORD_PARAM = "keyword";
	public static final String ID_PARAM = "id";
	
	public static final Gson GSON = new Gson();
	
	protected String url;
	protected String version;
	protected int timeOut;
	
	public HttpRequest(String url, String version, int timeout) {
		
		this.url = url;
		this.version = version;
		this.timeOut = timeout;
	}
	
	public HttpRequest(String url, int timeout) {
		this(url, DEFAULT_VERSION, timeout);
	}
	
	public HttpRequest(String url) {
		this(url, DEFAULT_VERSION, 5000);
	}

	/**
	 *
	 * @param path
	 * @param args
	 * @return
	 */
	protected String buildPath(String path, Object... args) {
		
		if(args == null)
			return path;
		
		return String.format(path, args);
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	public List<Entry<String, String>> getIdParam(Long id) {
		
		List<Entry<String, String>> params = new ArrayList<>();
		params.add(new CommonEntry<>(ID_PARAM, String.valueOf(id)));
		return params;
	}
}
