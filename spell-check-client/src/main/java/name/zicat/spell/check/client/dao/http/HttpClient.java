package name.zicat.spell.check.client.dao.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import name.zicat.utils.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.zicat.spell.check.client.utils.Constants;
import name.zicat.spell.check.client.utils.URLUtils;

/**
 * 
 * @author zicat
 *
 */
public class HttpClient implements IHttpClient {

	private static volatile CloseableHttpClient httpClient = null;
	private static volatile IdleConnectionMonitorThread idleConnectionMonitorThread = null;

	static {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(5000).build());
		cm.setDefaultMaxPerRoute(20);
		httpClient = HttpClients.custom().setConnectionManager(cm).build();
		idleConnectionMonitorThread = new IdleConnectionMonitorThread(cm);
		idleConnectionMonitorThread.start();
	}

	/**
	 *
	 */
	public static final void shutDown() {
		IOUtils.closeQuietly(httpClient);
	}

	static class IdleConnectionMonitorThread extends Thread {

		private static final Logger LOG = LoggerFactory.getLogger(IdleConnectionMonitorThread.class);
		
		private final HttpClientConnectionManager connMgr;
		private volatile boolean shutdown;

		public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
			super("Idle Connection Monitor Thread");
			this.connMgr = connMgr;
		}

		@Override
		public void run() {
			while (!shutdown) {
				synchronized (this) {
					try {
						connMgr.closeExpiredConnections();
						connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
						wait(5000);
					} catch (Exception e) {
						LOG.warn("expired connections error", e);
					}
				}
			}
		}

		/**
		 *
		 */
		public void shutdown() {
			shutdown = true;
			synchronized (this) {
				notifyAll();
			}
		}
	}

	private String urlString;
	private HttpHost httpHost;
	private int timeOut = 5000;

	public HttpClient(String urlString) throws MalformedURLException {
		this(urlString, 5000);
	}
	
	public HttpClient(String urlString, int timeOut) throws MalformedURLException {

		if (urlString.endsWith(Constants.SLASH))
			this.urlString = urlString.substring(0, urlString.length() - 1);
		else
			this.urlString = urlString;

		this.httpHost = URLUtils.build(urlString);
		this.timeOut = timeOut;
	}

	/**
	 * Close InputStream By self
	 * @param path
	 * @param params
	 * @param headers
	 * @return
	 * @throws IOException
	 */
	@Override
	public InputStream getStream(String path, List<Entry<String, String>> params, List<Header> headers)
			throws IOException {
		
		HttpRequestBase request = new HttpGet(buildFullPath(path, params));
		addHeader(request, headers);
		request.setConfig(RequestConfig.custom().setSocketTimeout(timeOut).build());
		CloseableHttpResponse resp = null;
		resp = httpClient.execute(httpHost, request);
		return resp.getEntity().getContent();
	}

	/**
	 *
	 * @param path
	 * @param params
	 * @param headers
	 * @return
	 * @throws IOException
	 */
	@Override
	public byte[] get(String path, List<Entry<String, String>> params, List<Header> headers) throws IOException {
		
		HttpRequestBase request = new HttpGet(buildFullPath(path, params));
		addHeader(request, headers);
		request.setConfig(RequestConfig.custom().setSocketTimeout(timeOut).build());
		CloseableHttpResponse resp = null;
		try {
			resp = httpClient.execute(httpHost, request);
			return EntityUtils.toByteArray(resp.getEntity());
		} finally {
			if(resp != null)
				resp.close();
		}
	}

	/**
	 *
	 * @param path
	 * @param params
	 * @param headers
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	@Override
	public String get(String path, List<Entry<String, String>> params, List<Header> headers, Charset charset) throws IOException {
		
		HttpRequestBase request = new HttpGet(buildFullPath(path, params));
		addHeader(request, headers);
		request.setConfig(RequestConfig.custom().setSocketTimeout(timeOut).build());
		CloseableHttpResponse resp = null;
		try {
			resp = httpClient.execute(httpHost, request);
			return EntityUtils.toString(resp.getEntity(), charset);
		} finally {
			if(resp != null)
				resp.close();
		}
	}

	/**
	 *
	 * @param path
	 * @param params
	 * @param headers
	 * @param entity
	 * @return
	 * @throws IOException
	 */
	@Override
	public byte[] post(String path, List<Entry<String, String>> params, List<Header> headers, HttpEntity entity)
			throws IOException {
		
		HttpPost request = new HttpPost(buildFullPath(path, params));
		addHeader(request, headers);
		request.setConfig(RequestConfig.custom().setSocketTimeout(timeOut).build());
		request.setEntity(entity);
		CloseableHttpResponse resp = null;
		try {
			resp = httpClient.execute(httpHost, request);
			return EntityUtils.toByteArray(resp.getEntity());
		} finally {
			if(resp != null)
				resp.close();
		}
	}

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
	@Override
	public String post(String path, List<Entry<String, String>> params, List<Header> headers, HttpEntity entity,
			Charset charset) throws IOException {
		
		HttpPost request = new HttpPost(buildFullPath(path, params));
		addHeader(request, headers);
		request.setConfig(RequestConfig.custom().setSocketTimeout(timeOut).build());
		request.setEntity(entity);
		CloseableHttpResponse resp = null;
		try {
			resp = httpClient.execute(httpHost, request);
			return EntityUtils.toString(resp.getEntity(), charset);
		} finally {
			if(resp != null)
				resp.close();
		}
	}
	
	/**
	 * 
	 * @param path
	 * @param params
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String buildFullPath(String path, List<Entry<String, String>> params) throws UnsupportedEncodingException {
		
		StringBuilder result = new StringBuilder(urlString);
		if(path != null) {
			
			if(path.endsWith(Constants.SLASH))
				path = path.substring(0, path.length() - 1);
			
			if (!path.startsWith(Constants.SLASH) && !path.isEmpty())
				result.append(Constants.SLASH);
			result.append(path);
		}
		
		if(params != null && !params.isEmpty()) {
			if(!result.toString().endsWith(Constants.QUESTION))
				result.append(Constants.QUESTION); 
			
			Iterator<Entry<String, String>> it = params.iterator();
			while(it.hasNext()) {
				Entry<String, String> param = it.next();
				result.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8.name()));
				result.append(Constants.EQUATION);
				result.append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8.name()));
				if(it.hasNext()) {
					result.append(Constants.AND);
				}
			}
		}
		return result.toString();
	}

	/**
	 *
	 * @param request
	 * @param headers
	 */
	private void addHeader(HttpRequest request, List<Header> headers) {

		if (request != null && headers != null) {
			for (Header header : headers) {
				if (header != null) {
					request.addHeader(header);
				}
			}
		}
	}
}
