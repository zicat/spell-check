package name.zicat.spell.check.biz.conf;

import java.io.File;
import java.io.IOException;

import name.zicat.spell.check.biz.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author zicat
 *
 * @param <C>
 */
public class ConfigurationListener<C> extends Thread {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationListener.class);
	private volatile C configuration;
	private final File file;
	private int scanTime = 3000;
	private String md5;
	
	public ConfigurationListener(C configuration, File file, int scanTime) throws IOException {
		
		super("Configuration Scan Thread-" + file.getName().toUpperCase());
		super.setDaemon(true);
		
		if(configuration == null)
			throw new NullPointerException("configuration is null");
		
		this.configuration = configuration;
		this.file = file;
		this.md5 = SecurityUtils.md5(file);
		if(scanTime > 0)
			this.scanTime = scanTime;
	}
	
	public C getConfiguration() {
		return configuration;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		
		while(true) {
			try {
				Thread.sleep(scanTime);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("ScanTime Thread Interrupted", e);
			}
			
			String md5;
			try {
				md5 = SecurityUtils.md5(file);
				if(!md5.equals(this.md5)) {
					this.configuration = (C) ConfigurationManager.initConfig(file.getName(), configuration.getClass());
					this.md5 = md5;
				}
			} catch (Exception e) {
				LOG.warn("Check Change File Error, FileName = " + file.getName(), e);
			}
		}
	}
	
}
