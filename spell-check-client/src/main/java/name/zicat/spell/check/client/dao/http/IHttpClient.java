package name.zicat.spell.check.client.dao.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

/**
 * 
 * @author zicat
 *
 */
public interface IHttpClient {
	
	/**
	 * 
	 * @param path
	 * @param params
	 * @param headers
	 * @return
	 * @throws IOException
	 */
	byte[] get(String path, List<Entry<String, String>> params, List<Header> headers) throws IOException;
	
	/**
	 * 
	 * @param path
	 * @param params
	 * @param headers
	 * @return
	 * @throws IOException
	 */
	InputStream getStream(String path, List<Entry<String, String>> params, List<Header> headers) throws IOException;
	
	/**
	 * 
	 * @param path
	 * @param params
	 * @param headers
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	String get(String path, List<Entry<String, String>> params, List<Header> headers, Charset charset) throws IOException;
	
	
	
	/**
	 * 
	 * @param path
	 * @param params
	 * @param headers
	 * @param entity
	 * @return
	 * @throws IOException
	 */
	byte[] post(String path, List<Entry<String, String>> params, List<Header> headers, HttpEntity entity) throws IOException;
	
	/**
	 * 
	 * @param path
	 * @param params
	 * @param headers
	 * @param entity
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	String post(String path, List<Entry<String, String>> params, List<Header> headers, HttpEntity entity, Charset charset) throws IOException;
}
