package name.zicat.spell.check.client.query;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;

import name.zicat.spell.check.client.dao.http.HttpClient;
import name.zicat.spell.check.client.dao.http.IHttpClient;

/**
 * 
 * @author zicat
 *
 */
public class DownloadRequest extends HttpRequest {
	
	public DownloadRequest(String url, String version, int timeOut) {
		super(url, version, timeOut);
	}
	
	public DownloadRequest(String url, int timeOut) {
		super(url, timeOut);
	}
	
	public DownloadRequest(String url) {
		super(url);
	}
	/**
	 * # Close InputStream by user
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public InputStream download(Long id) throws IOException {
		
		IHttpClient client = new HttpClient(url, timeOut);
		String path = buildPath(DOWNLOAD_PATH, version);
		List<Entry<String, String>> params = getIdParam(id);
		return client.getStream(path, params, null);
	}
	
}
