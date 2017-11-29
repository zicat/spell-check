package name.zicat.spell.check.core.test;

import org.junit.Test;

import name.zicat.spell.check.core.NSuggestWordScoreComparator;
import name.zicat.spell.check.core.model.SuggestWordResult;

import junit.framework.Assert;

/**
 * @author zicat
 */
public class NSuggestWordScoreComparatorTest {
	
	@Test
	public void test() {
		
		NSuggestWordScoreComparator comparator = new NSuggestWordScoreComparator();
		
		SuggestWordResult s1 = new SuggestWordResult();
		s1.string = "aa";
		s1.freq = 100;
		s1.distanceCount = 3;
		s1.clickCount = 50;
		
		SuggestWordResult s2 = new SuggestWordResult();
		s2.string = "bb";
		s2.freq = 100;
		s2.distanceCount = 1;
		s2.clickCount = 60;
		
		Assert.assertEquals(comparator.compare(s1, s2), 1);
		
		s2.distanceCount = 2;
		Assert.assertEquals(comparator.compare(s1, s2), 1);
		
		s2.distanceCount = 3;
		Assert.assertEquals(comparator.compare(s1, s2), s2.clickCount - s1.clickCount);
		
		s2.distanceCount = 6;
		Assert.assertEquals(comparator.compare(s1, s2), -1);
	}
}
