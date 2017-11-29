package name.zicat.spell.check.biz.test.service.localfile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import name.zicat.spell.check.biz.service.localfile.NSpellCheckerFactory;
import name.zicat.spell.check.biz.service.localfile.SCLoadService;
import name.zicat.spell.check.core.NSpellChecker;
import name.zicat.utils.file.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * 
 * @author zicat
 *
 */
public class SCLoadServiceTest {
	
	File dir;
	
	@Before
	public void before() throws IOException {
		dir = new File("./spell_check_test");
		FileUtils.createDirIfNeed(dir);
		FileUtils.cleanUpDir(dir);
		File f = new File(dir, "0");
		f.mkdir();
		f = new File(dir, "1");
		f.mkdir();
		f = new File(dir, "2");
		f.mkdir();
		f = new File(dir, "asdf");
		f.mkdir();
	}
	
	@After
	public void after() {
		
		FileUtils.cleanUpDir(dir);
		dir.delete();
	}
	
	@Test
	public void test() throws Exception {
		
		SCLoadService service = new SCLoadService(dir.getCanonicalPath(), NSpellCheckerFactory.DEFAULT);
		Map<Long, NSpellChecker> spellcheckers = service.load();
		Assert.assertEquals(spellcheckers.size(), 3);
		
		try {
			spellcheckers = service.load(false);
			Assert.assertTrue(false);
		} catch(Throwable e) {
			Assert.assertTrue(true);
		}
		
		try {
			Map<Long, NSpellChecker> spellcheckers2 = service.load();
			Assert.assertTrue(true);
			Assert.assertTrue(spellcheckers2.isEmpty());
		} catch(Throwable e) {
			Assert.assertTrue(false);
		}
		
		
		for(Map.Entry<Long, NSpellChecker> entry: spellcheckers.entrySet()) {
			entry.getValue().close();
		}
		
		FileUtils.cleanUpDir(dir);
		spellcheckers = service.load();
		Assert.assertNull(spellcheckers);
		dir.delete();
		spellcheckers = service.load();
		Assert.assertNull(spellcheckers);
	}
}
