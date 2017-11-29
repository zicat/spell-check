package name.zicat.spell.check.biz.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * 
 * @author zicat
 *
 */
public class MyApplication extends Application {

	/**
	 *
	 * @return
	 */
	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(MultiPartFeature.class);
		return classes;
	}
}
