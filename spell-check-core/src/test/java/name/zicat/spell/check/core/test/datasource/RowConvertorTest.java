package name.zicat.spell.check.core.test.datasource;

import org.junit.Test;

import name.zicat.spell.check.core.datasource.Record;
import name.zicat.spell.check.core.datasource.RowConvertor;

import junit.framework.Assert;

/**
 * @author zicat
 */
public class RowConvertorTest {
	
	@Test
	public void testDefaultValue() {
		
		RowConvertor convertor = RowConvertor.defaultValue("\\|");
		Record record = convertor.convert("aa|11");
		Assert.assertEquals(record.getKeyword(), "aa");
		Assert.assertEquals(record.getClickCount(), 11);
	}
}
