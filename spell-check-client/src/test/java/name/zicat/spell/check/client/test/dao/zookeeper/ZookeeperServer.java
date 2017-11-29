package name.zicat.spell.check.client.test.dao.zookeeper;

import java.io.IOException;

import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;


/**
 * 
 * @author zicat
 *
 */
public class ZookeeperServer {
	
	private TestingServer testingServer;

	@Before
	public void initZK() throws Exception {
		
		testingServer = new TestingServer();
		testingServer.start();
	}
	
	public String getHostPort() {
		return testingServer.getConnectString();
	}
	
	@After
	public void closeZK() throws IOException {
		testingServer.close();
	}
}
