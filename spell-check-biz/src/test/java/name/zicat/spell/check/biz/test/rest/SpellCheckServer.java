package name.zicat.spell.check.biz.test.rest;

import name.zicat.spell.check.biz.conf.ConfigurationManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author zicat
 */
public class SpellCheckServer {
	
	private Server server = null;
	
	public  void start() throws Exception {
		
		server = new Server(ConfigurationManager.getSpellCheckConfig().getServicePort());
		WebAppContext webAppContext = new WebAppContext("src/main/webapp", "/spellcheck");
		server.setHandler(webAppContext);
		server.start();
		System.out.println("started");
	}
	
	public void stop() throws Exception {
		server.stop();
	}
}
