package name.zicat.spell.check.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zicat
 */
public class Longs {
	
	private static final Logger LOG = LoggerFactory.getLogger(Longs.class);

	/**
	 *
	 * @param n
	 * @param defaultValue
	 * @return
	 */
	public static long valueOf(String n, Long defaultValue) {
		
		Long result = defaultValue;
		try {
			result = Long.valueOf(n);
		} catch(Exception e) {
			LOG.warn("convert string " + n + " to long error, use default value " + defaultValue + ", error details:", e);
		}
		return result;
	}
}
