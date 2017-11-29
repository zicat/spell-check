package name.zicat.spell.check.biz.service.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import name.zicat.spell.check.biz.cache.ZookeeperLocalCache;
import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.conf.spellcheck.IndexConfig;
import name.zicat.spell.check.biz.service.localfile.NSpellCheckerFactory;
import name.zicat.spell.check.biz.service.zookeeper.RandomAvailableInstanceByVersion;
import name.zicat.spell.check.biz.utils.ZipUtil;
import name.zicat.spell.check.client.query.DownloadRequest;
import name.zicat.spell.check.core.NSpellChecker;
import name.zicat.utils.file.FileUtils;
import name.zicat.utils.io.IOUtils;

/**
 * 
 * @author zicat
 *
 */
public class FetchService {
	
	private Long version;
	private String tempPath;
	private String indexPath;
	
	public FetchService(Long version) throws Exception {
		
		IndexConfig indexConfig = ConfigurationManager.getSpellCheckConfig().getIndexConfig();
		this.version = version;
		this.tempPath = indexConfig.getTempPath();
		this.indexPath = indexConfig.getIndexPath();
	}
	
	public FetchService(Long version, String tempPath, String indexPath) {
		this.version = version;
		this.tempPath = tempPath;
		this.indexPath = indexPath;
	}
	
	public NSpellChecker fetch() throws Exception {
		
		if(ZookeeperLocalCache.getCache().containsSpellChecker(version)) {
			return ZookeeperLocalCache.getCache().getSpellChecker(version);
		}
		
		InputStream in = null;
		FileOutputStream os = null;
		try {
			
			File tempDir = new File(tempPath).getCanonicalFile();
			File indexDir = new File(indexPath).getCanonicalFile();
			synchronized (FetchService.class) {
				FileUtils.createDirIfNeed(tempDir);
				FileUtils.createDirIfNeed(indexDir);
			}
			
			File tempIndexFile = File.createTempFile("index-zip-" + version + "-", "zip", tempDir);
			tempIndexFile.createNewFile();

			/** Get Available Version from Other Instances by Zookeeper **/
			RandomAvailableInstanceByVersion service = new RandomAvailableInstanceByVersion();
			String ipAndPort = service.random(version);
			DownloadRequest request = new DownloadRequest(ipAndPort);
			in = request.download(version);
			
			if(in == null) 
				return null;
			
			try {
				os = new FileOutputStream(tempIndexFile);
				byte[] buffer = new byte[1024];
				int len = 0;
				while((len = in.read(buffer)) != -1) {
					os.write(buffer, 0, len);
				}
				os.flush();
			} finally {
				IOUtils.closeQuietly(os);
			}
			File versionIndexDir = new File(indexDir, String.valueOf(version));
			synchronized (FetchService.class) {
				FileUtils.createDirIfNeed(versionIndexDir);
			}
			File zipFile = new File(indexDir, version + ZipUtil.SUFFIX_ZIP);
			org.apache.commons.io.FileUtils.copyFile(tempIndexFile, zipFile);
			tempIndexFile.delete();
			ZipUtil.decompressZip(zipFile, versionIndexDir);
			NSpellChecker nSpellChecker =  NSpellCheckerFactory.DEFAULT.create(versionIndexDir);
			ZookeeperLocalCache.getCache().setSpellchecker(version, nSpellChecker);
			return  nSpellChecker;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}
