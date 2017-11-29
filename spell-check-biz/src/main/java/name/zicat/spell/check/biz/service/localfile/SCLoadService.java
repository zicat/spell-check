package name.zicat.spell.check.biz.service.localfile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import name.zicat.spell.check.core.NSpellChecker;
import name.zicat.utils.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.zicat.spell.check.client.model.IndexInfo;

/**
 * 
 * @author zicat
 *
 */
public class SCLoadService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SCLoadService.class);
	private String indexPath;
	private NSpellCheckerFactory nSpellCheckFactory;
	
	public SCLoadService(String indexPath, NSpellCheckerFactory nSpellCheckFactory) {
		
		this.indexPath = indexPath;
		this.nSpellCheckFactory = nSpellCheckFactory;
	}
	
	public SCLoadService(String indexPath) {
		this(indexPath, NSpellCheckerFactory.DEFAULT);
	}
	
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<Long, NSpellChecker> load() throws Exception {
		return load(true);
	}
	
	/**
	 * 
	 * @param quiet
	 * @return
	 * @throws Exception
	 */
	public Map<Long, NSpellChecker> load(boolean quiet) throws Exception {
		
		File file = new File(indexPath);
		if(!file.exists() || !file.isDirectory())
			return null;
		
		File[] dicDirs = file.listFiles(pathname -> pathname.isDirectory() && IndexInfo.idValid(pathname.getName()));
		
		if(dicDirs == null || dicDirs.length == 0)
			return null;
		
		Map<Long, NSpellChecker> checkerList = new HashMap<>();
		for(File dicDir: dicDirs) {
			
			try {
				checkerList.put(Long.valueOf(dicDir.getName()), nSpellCheckFactory.create(dicDir));
			} catch (Exception e) {
				if(quiet) {
					LOG.error("Load Dir " + dicDir.getAbsolutePath() + " failed! Quiet Load, Don't worry", e);
				} else {
					for(Map.Entry<Long, NSpellChecker> entry: checkerList.entrySet()) {
						IOUtils.closeQuietly(entry.getValue());
					}
					checkerList.clear();
					throw e;
				}
			}
		}
		return checkerList;
	}
}
