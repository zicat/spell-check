package name.zicat.spell.check.biz.service.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;

import name.zicat.spell.check.biz.service.localfile.NSpellCheckerFactory;
import name.zicat.spell.check.core.NSpellChecker;
import name.zicat.spell.check.core.datasource.RowConvertor;
import name.zicat.spell.check.core.datasource.txt.TXTLineSourceReader;
import name.zicat.utils.file.FileUtils;
import name.zicat.utils.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.zicat.spell.check.biz.cache.ZookeeperLocalCache;
import name.zicat.spell.check.biz.conf.ConfigurationManager;
import name.zicat.spell.check.biz.conf.spellcheck.IndexConfig;
import name.zicat.spell.check.client.model.IndexInfo;
import name.zicat.spell.check.client.model.ServiceInstance;
import name.zicat.spell.check.biz.service.zookeeper.InstancesByIndexVersionService;
import name.zicat.spell.check.biz.service.zookeeper.register.IndexRegisterService;
import name.zicat.spell.check.biz.utils.ZipUtil;
import name.zicat.spell.check.client.model.Response;
import name.zicat.spell.check.client.utils.Constants;

/**
 *
 * @author zicat
 *
 */
public class UploadService {
	
	private static final Logger LOG = LoggerFactory.getLogger(UploadService.class);
	
	private Long version;

	public UploadService(Long version) {
		this.version = version;
	}

	/**
	 *
	 * @param in
	 * @return
	 */
	public Response<String> upload(InputStream in) {
		
		synchronized (UploadService.class) {
			
			Response<String> response = new Response<>();
			try {
				InstancesByIndexVersionService service = new InstancesByIndexVersionService();
				if(service.getInstanceByIndexVersion(version) != null) {
					response.setCode(Constants.HTTP_STATUS_ERROR);
					response.setMessage("Version Exist!! " + version);
					return response;
				}
				
				IndexConfig indexConfig = ConfigurationManager.getSpellCheckConfig().getIndexConfig();
				File tempDir = new File(indexConfig.getTempPath()).getCanonicalFile();
				File indexDir = new File(indexConfig.getIndexPath()).getCanonicalFile();
				
				FileUtils.createDirIfNeed(tempDir);
				
				FileUtils.createDirIfNeed(indexDir);
				File versionIndexDir = new File(indexDir, String.valueOf(version));
				FileUtils.createDirIfNeed(versionIndexDir);
				FileUtils.cleanUpDir(versionIndexDir);
				
				File file = File.createTempFile("dictionary-" + version + "-", ".dic", tempDir);
				file.createNewFile();

				FileOutputStream os = null;
				try {
					os = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int len;
					while((len = in.read(buffer)) != -1) {
						os.write(buffer, 0, len);
					}
					os.flush();
				} finally {
					IOUtils.closeQuietly(in, os);
				}
				
				Reader reader = null;
				NSpellChecker spellChecker = null;
				try {
					reader = new FileReader(file);
					spellChecker = NSpellCheckerFactory.DEFAULT.create(versionIndexDir);
					spellChecker.indexDictionary(new TXTLineSourceReader(reader, RowConvertor.defaultValue("\u0001")), true);
				} finally {
					//zip need to close file handler, so zip after close spell checker instance
					IOUtils.closeQuietly(reader, spellChecker);
				}
				
				ZipUtil.compressFiles2Zip(versionIndexDir.listFiles(), new File(indexDir, version + ZipUtil.SUFFIX_ZIP));
				
				//reopen
				spellChecker = NSpellCheckerFactory.DEFAULT.create(versionIndexDir);
				ZookeeperLocalCache.getCache().setSpellchecker(version, spellChecker);
				IndexRegisterService indexRegisterService = new IndexRegisterService();
				
				ServiceInstance serviceInstance = ConfigurationManager.getSpellCheckConfig().create();
				indexRegisterService.register(new IndexInfo(serviceInstance, version), null);
				response.setMessage("Upload Successful!!! Version is " + version + ", Instance Id is " + serviceInstance);
				return response;
			} catch(Exception e) {
				response.setCode(Constants.HTTP_STATUS_ERROR);
				response.setMessage(e.toString());
				LOG.error("upload service exception, version = " + version, e);
			}
			return response;
		}
	}
}
