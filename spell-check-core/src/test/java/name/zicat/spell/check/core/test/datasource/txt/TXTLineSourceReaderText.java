package name.zicat.spell.check.core.test.datasource.txt;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import name.zicat.spell.check.core.datasource.DataSourceReader;
import name.zicat.spell.check.core.datasource.Record;
import name.zicat.spell.check.core.datasource.RowConvertor;
import name.zicat.spell.check.core.datasource.txt.TXTLineSourceReader;

/**
 * 
 * @author zicat
 *
 */
public class TXTLineSourceReaderText {
	
	Reader r  = null;
	
	@Before
	public void before() {
		r = new StringReader("samsung ssds|100\n" + "samsung ssd|2000\n" + "samsung|10");
	}
	
	@Test
	public void test() throws IOException {
		
		DataSourceReader sourceReader = new TXTLineSourceReader(r, RowConvertor.defaultValue("\\|"));
		
		Assert.assertTrue(sourceReader.hasNext());
		Record record = sourceReader.next();
		Assert.assertEquals(record.getKeyword(), "samsung ssds");
		Assert.assertEquals(record.getClickCount(), 100);
		
		Assert.assertTrue(sourceReader.hasNext());
		record = sourceReader.next();
		Assert.assertEquals(record.getKeyword(), "samsung ssd");
		Assert.assertEquals(record.getClickCount(), 2000);
		
		Assert.assertTrue(sourceReader.hasNext());
		record = sourceReader.next();
		Assert.assertEquals(record.getKeyword(), "samsung");
		Assert.assertEquals(record.getClickCount(), 10);
		
		
		Assert.assertTrue(!sourceReader.hasNext());
		Assert.assertNull(sourceReader.next());
	}
	
	@After
	public void after() throws IOException {
		r.close();
	}
}
