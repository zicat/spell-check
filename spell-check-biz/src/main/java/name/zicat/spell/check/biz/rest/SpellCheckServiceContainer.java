package name.zicat.spell.check.biz.rest;

import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.WebConfig;

import name.zicat.spell.check.biz.service.boot.BootService;

import javax.servlet.ServletException;

/**
 *  @author zicat
 */
public class SpellCheckServiceContainer extends ServletContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4420675284218951424L;

	@Override
	protected void init(WebConfig webConfig) throws ServletException {
		
		super.init(webConfig);
		try {
			BootService.boot();
		} catch (Exception e) {
			throw new ServletException("Boot Fail", e);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
	}

}
