package name.zicat.spell.check.client.test.dao.http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import name.zicat.utils.ds.map.CommonEntry;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.message.BasicHeader;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import name.zicat.spell.check.client.dao.http.HttpClient;
import name.zicat.spell.check.client.dao.http.IHttpClient;

/**
 * 
 * @author zicat
 *
 */
public class HttpClientTest {
	
	public static HttpServer server;
	
	@BeforeClass
	public static void before() throws IOException {
		final SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(15000).build();
		server = ServerBootstrap.bootstrap().setSocketConfig(socketConfig).setServerInfo("TEST/1.1").registerHandler("/echo/*", new EchoHandler()).create();
		server.start();
	}
	
	@AfterClass
	public static void after() {
		
		HttpClient.shutDown();
		if(server != null)
			server.shutdown(5, TimeUnit.SECONDS);
	}
	
	@Test
	public void testGetAndPost() throws IOException {
		
		IHttpClient client = new HttpClient("http://localhost:" + server.getLocalPort());
		String v = client.get("/echo/aa", null, null, StandardCharsets.UTF_8);
		byte[] bs = client.get("/echo/aa", null, null);
		Assert.assertTrue(v.isEmpty());
		Assert.assertTrue(bs.length == 0);
		
		v = client.post("/echo/aa", null, null, new StringEntity("values"), StandardCharsets.UTF_8);
		Assert.assertTrue(v.equals("values"));
		
		bs = client.post("/echo/aa", null, null, new StringEntity("values"));
		v = new String(bs, StandardCharsets.UTF_8);
		Assert.assertTrue(v.equals("values"));
	}
	
	@Test
	public void testAddHeader() throws MalformedURLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		IHttpClient client = new HttpClient("http://localhost:" + server.getLocalPort());
		Method method = HttpClient.class.getDeclaredMethod("addHeader", new Class[]{HttpRequest.class, List.class});
		method.setAccessible(true);
		HttpRequest request = new HttpGet();
		List<Header> headers = null;
		method.invoke(client, request, headers);
		Assert.assertTrue(request.getAllHeaders() == null || request.getAllHeaders().length == 0);
		headers = new ArrayList<>();
		headers.add(new BasicHeader("aa", "11"));
		headers.add(new BasicHeader("bb", "22"));
		method.invoke(client, request, headers);
		Assert.assertTrue(request.getAllHeaders() != null && request.getAllHeaders().length == 2);
		Assert.assertTrue(request.getHeaders("aa")[0].getValue().equals("11"));
		Assert.assertTrue(request.getHeaders("bb")[0].getValue().equals("22"));
		
		method = HttpClient.class.getDeclaredMethod("buildFullPath", new Class[]{String.class, List.class});
		method.setAccessible(true);
		String fullPath = (String) method.invoke(client, null, null);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort()));
		fullPath = (String) method.invoke(client, "a", null);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort() + "/a"));
		fullPath = (String) method.invoke(client, "/a/", null);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort() + "/a"));
		fullPath = (String) method.invoke(client, "/", null);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort()));
		
		List<Entry<String, String>> params = new ArrayList<>();
		fullPath = (String) method.invoke(client, null, params);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort()));
		fullPath = (String) method.invoke(client, "a", params);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort() + "/a"));
		fullPath = (String) method.invoke(client, "/a/", params);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort() + "/a"));
		fullPath = (String) method.invoke(client, "/", params);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort()));
		
		params.add(new CommonEntry<String, String>("aa", "bb"));
		params.add(new CommonEntry<String, String>("cc", "dd"));
		fullPath = (String) method.invoke(client, null, params);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort() + "?aa=bb&cc=dd"));
		fullPath = (String) method.invoke(client, "a", params);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort() + "/a?aa=bb&cc=dd"));
		fullPath = (String) method.invoke(client, "/a/", params);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort() + "/a?aa=bb&cc=dd"));
		fullPath = (String) method.invoke(client, "/", params);
		Assert.assertTrue(fullPath.equals("http://localhost:" + server.getLocalPort() + "?aa=bb&cc=dd"));
	}
}
