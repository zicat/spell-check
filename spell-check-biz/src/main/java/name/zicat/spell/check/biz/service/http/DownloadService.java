package name.zicat.spell.check.biz.service.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.conf.spellcheck.IndexConfig;
import name.zicat.spell.check.biz.utils.ZipUtil;
import name.zicat.utils.file.FileUtils;

/**
 * 
 * @author zicat
 *
 */
public class DownloadService {
	
	private Long version;

	public DownloadService(Long version) {
		this.version = version;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public InputStream download() throws Exception {
		
		IndexConfig indexConfig = ConfigurationManager.getSpellCheckConfig().getIndexConfig();
		File indexDir = new File(indexConfig.getIndexPath()).getCanonicalFile();
		synchronized (DownloadService.class) {
			FileUtils.createDirIfNeed(indexDir);
		}
		
		File files[] = indexDir.listFiles(pathname -> pathname.getName().equals(version + ZipUtil.SUFFIX_ZIP));
		
		if(files != null && files.length == 1) {
			return new FileInputStream(files[0]);
		}
		
		throw new Exception("Instance " + ConfigurationManager.getSpellCheckConfig().create() + " fail to find " + version + " index on local");
	}
}
