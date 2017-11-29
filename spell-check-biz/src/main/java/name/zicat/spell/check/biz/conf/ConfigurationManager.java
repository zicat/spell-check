package name.zicat.spell.check.biz.conf;

import name.zicat.spell.check.biz.conf.spellcheck.Configuration;
import name.zicat.utils.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zicat
 * @date 2017/06/22
 */
public class ConfigurationManager {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);
	private static ConcurrentHashMap<String, ConfigurationListener<?>> cache = new ConcurrentHashMap<>();

	/**
	 *
	 * @return
	 * @throws JAXBException
	 * @throws URISyntaxException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static Configuration getSpellCheckConfig() throws JAXBException, URISyntaxException, NoSuchAlgorithmException, IOException {
		return getConfiguration(Configuration.ROOT_FILE, Configuration.class);
	}

	/**
	 *
	 * @param fileName
	 * @param clazz
	 * @param <T>
	 * @return
	 * @throws JAXBException
	 * @throws URISyntaxException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T>  T getConfiguration(String fileName, Class<T> clazz) throws JAXBException, URISyntaxException, NoSuchAlgorithmException, IOException {

		if (!cache.containsKey(fileName)) {
			
			synchronized (ConfigurationManager.class) {

				if (!cache.containsKey(fileName)) {
					T configuration = initConfig(fileName, clazz);
					URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
					File file = new File(url.toURI());
					ConfigurationListener<?> listener = new ConfigurationListener(configuration, file, 5000);
					listener.start();
					cache.put(fileName, listener);
					return configuration;
				}
			}
		}

		return (T) cache.get(fileName).getConfiguration();
	}

	/**
	 *
	 * @param fileName
	 * @param clazz
	 * @param <T>
	 * @return
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	static <T> T initConfig(final String fileName, Class<T> clazz) throws JAXBException {
		LOG.info("Initializing spell check config...");
		InputStream is = null;
		try {
			JAXBContext context  = JAXBContext.newInstance(clazz);
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
			Unmarshaller um = context.createUnmarshaller();
			return (T) um.unmarshal(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
}
