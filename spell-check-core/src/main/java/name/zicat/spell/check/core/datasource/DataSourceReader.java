package name.zicat.spell.check.core.datasource;

import java.io.IOException;

/**
 * 
 * @author zicat
 *
 */
public interface DataSourceReader {

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	boolean hasNext() throws IOException;

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	Record next() throws IOException;
}
