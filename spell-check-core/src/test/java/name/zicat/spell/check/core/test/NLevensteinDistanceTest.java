package name.zicat.spell.check.core.test;

import name.zicat.spell.check.core.NLevensteinDistance;
import name.zicat.spell.check.core.NStringDistance;
import name.zicat.spell.check.core.model.DistanceResult;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.StringDistance;
import org.junit.Test;

import name.zicat.spell.check.core.utils.Strings;

import junit.framework.Assert;

/**
 * @author zicat
 */
public class NLevensteinDistanceTest {
	
	@Test
	public void test() {
		
		NStringDistance sd = new NLevensteinDistance(new LevensteinDistance());
		StringDistance sd2 = new LevensteinDistance();
		
		DistanceResult f1 = sd.getDistance("amd fx", "fx zmd");
		float f2 = sd2.getDistance(Strings.normalize("amd fx", true), Strings.normalize("fx zmd", true));
		Assert.assertEquals(f1.getScore(), f2);
		
		
		f1 = sd.getDistance("amd fx", "amm fx");
		f2 = sd2.getDistance(Strings.normalize("amd fx", false), Strings.normalize("amm fx", false));
		Assert.assertEquals(f1.getScore(), f2);
	}
}
