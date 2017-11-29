package name.zicat.spell.check.client.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpHost;

/**
 * 
 * @author zicat
 *
 */
public class URLUtils {
	
	/**
	 * 
	 * @param urlString
	 * @return
	 * @throws MalformedURLException
	 */
	public static HttpHost build(String urlString) throws MalformedURLException {
		
		URL url = new URL(urlString);
		StringBuilder sb = new StringBuilder();
		sb.append(url.getProtocol());
		sb.append(Constants.COLON);
		sb.append(Constants.SLASH);
		sb.append(Constants.SLASH);
		sb.append(url.getHost());
		sb.append(Constants.COLON);
		sb.append(url.getPort());
		return HttpHost.create(sb.toString());
	}
}
