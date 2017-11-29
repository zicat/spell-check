package name.zicat.spell.check.core.test.utils;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import name.zicat.spell.check.core.utils.Strings;

import junit.framework.Assert;

/**
 * 
 * @author lz31
 *
 */
public class StringsTest {
	
	@Test
	public void test() {
		
		Assert.assertEquals(Strings.normalize(null), null);
		Assert.assertEquals(Strings.normalize("aa"), "aa");
		Assert.assertEquals(Strings.normalize("aa bb"), "aa bb");
		Assert.assertEquals(Strings.normalize("bb aa"), "aa bb");
		
		Assert.assertEquals(Strings.termCount(null), 0);
		Assert.assertEquals(Strings.termCount(" "), 0);
		Assert.assertEquals(Strings.termCount("aaa sdfsdf   fasdff"), 3);
		
		Assert.assertEquals(Strings.formGrams("aa", 3), null);
		
		String[] grams = Strings.formGrams("aa", 2);
		Assert.assertEquals(grams.length, 1);
		Assert.assertEquals(grams[0], "aa");
		
		grams = Strings.formGrams("abcd", 2);
		Assert.assertEquals(grams.length, 3);
		Assert.assertEquals(grams[0], "ab");
		Assert.assertEquals(grams[1], "bc");
		Assert.assertEquals(grams[2], "cd");
		
		Assert.assertTrue(Strings.containsNumeric("asfasdf0asdfasdf"));
		Assert.assertTrue(Strings.containsNumeric("asfasdf9asdfasdf"));
		Assert.assertFalse(Strings.containsNumeric("asfasdfaasdfasdf"));
		Assert.assertFalse(Strings.containsNumeric(null));
		Assert.assertFalse(Strings.containsNumeric(""));
		
		Assert.assertEquals(Strings.charCount(null), null);
		Assert.assertEquals(Strings.charCount(""), null);
		Map<Character, Integer> charCount = Strings.charCount("assf121adf");
		Assert.assertEquals(charCount.size(), 6);
		Assert.assertTrue(charCount.get(new Character('1')).intValue() == 2);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		IndexWriter writer = new IndexWriter(new RAMDirectory(), new IndexWriterConfig(new StandardAnalyzer()));
		writer.commit();
		
		WriterThread t1 = new WriterThread(writer, false);
		WriterThread t2 = new WriterThread(writer, true);
		
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		writer.close();
		
	}
	
	
	static class WriterThread extends Thread {
		
		IndexWriter writer;
		boolean merge;
		
		public WriterThread(IndexWriter writer, boolean merge) {
			this.writer = writer;
			this.merge = merge;
		}
		
		@Override
		public void run() {
			Document document = new Document();
			Field f = new StringField("ff", "vv", Field.Store.YES);
			document.add(f);
			try {
				writer.addDocument(document);
				if(merge)
					writer.forceMerge(1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
