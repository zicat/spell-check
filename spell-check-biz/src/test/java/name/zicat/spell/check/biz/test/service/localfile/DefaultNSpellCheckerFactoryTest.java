package name.zicat.spell.check.biz.test.service.localfile;

import java.io.File;
import java.io.IOException;

import name.zicat.spell.check.biz.service.localfile.NSpellCheckerFactory;
import name.zicat.spell.check.core.NSpellChecker;
import name.zicat.utils.file.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * 
 * @author zicat
 *
 */
public class DefaultNSpellCheckerFactoryTest {
	
	File dir;
	
	@Before
	public void before() throws IOException {
		dir = new File("./spell_check_test");
		FileUtils.createDirIfNeed(dir);
		FileUtils.cleanUpDir(dir);
	}
	
	@After
	public void after() {
		
		FileUtils.cleanUpDir(dir);
		dir.delete();
	}
	
	@Test
	public void test() throws IOException {
		
		NSpellCheckerFactory nspellCheckerFactory = NSpellCheckerFactory.DEFAULT;
		File dir = new File("./spell_check_test");
		FileUtils.createDirIfNeed(dir);
		NSpellChecker spellChecker = nspellCheckerFactory.create(dir);
		spellChecker.close();
	}
}
