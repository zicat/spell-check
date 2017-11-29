package name.zicat.spell.check.core.test.utils;

import org.junit.Test;

import name.zicat.spell.check.core.utils.Longs;

import junit.framework.Assert;

public class LongsTest {
	
	@Test
	public void test() {
		
		Assert.assertEquals(Longs.valueOf("aa", 2L), 2);
		Assert.assertEquals(Longs.valueOf("11", 12L), 11);
	}
}
