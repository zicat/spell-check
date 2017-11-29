package name.zicat.spell.check.biz.service.localfile;

import name.zicat.spell.check.core.NSpellChecker;

import java.io.File;
import java.io.IOException;

/**
 * @ThreadSafe
 * @author zicat
 *
 */
public interface NSpellCheckerFactory {
	
	/**
	 * Default Implement
	 */
	NSpellCheckerFactory DEFAULT = new DefaultNSpellCheckerFactory();
	
	/**
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	NSpellChecker create(File dir) throws IOException;
}
