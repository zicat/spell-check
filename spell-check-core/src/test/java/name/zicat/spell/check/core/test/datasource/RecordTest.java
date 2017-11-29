package name.zicat.spell.check.core.test.datasource;

import org.junit.Test;

import name.zicat.spell.check.core.datasource.Record;

import junit.framework.Assert;

/**
 * 
 * @author zicat
 *
 */
public class RecordTest {
	
	@Test
	public void test() {
		Record r1 = new Record("aa", -1);
		Assert.assertEquals(r1.getKeyword(), "aa");
		Assert.assertEquals(r1.getClickCount(), 0);
		
		r1 = new Record("aa", 0);
		Assert.assertEquals(r1.getKeyword(), "aa");
		Assert.assertEquals(r1.getClickCount(), 0);
		
		r1 = new Record("aa", 1);
		Assert.assertEquals(r1.getKeyword(), "aa");
		Assert.assertEquals(r1.getClickCount(), 1);
		
		try {
			r1 = new Record(null, -1);
			Assert.assertTrue(false);
		} catch(NullPointerException e) {
			Assert.assertTrue(true);
		}
	}
}
