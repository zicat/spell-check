package name.zicat.spell.check.core.test;

import java.io.IOException;
import java.io.InputStreamReader;

import name.zicat.spell.check.core.utils.SpellCheckResultSplit;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import name.zicat.spell.check.core.NSpellChecker;
import name.zicat.spell.check.core.analyzer.SpellCheckStoreAnalyzer;
import name.zicat.spell.check.core.datasource.RowConvertor;
import name.zicat.spell.check.core.datasource.txt.TXTLineSourceReader;

import junit.framework.Assert;

/**
 * 
 * @author lz31
 *
 */
public class NSpellCheckerTest {
	
	private Directory directory = new RAMDirectory();
	private IndexWriterConfig config = new IndexWriterConfig(new SpellCheckStoreAnalyzer(NSpellChecker.PAYLOAD_LIMIT));
	private String dirct = "dictionary.dic";
	private String dirct2 = "dictionary2.dic";
	private String dirct3 = "dictionary3.dic";
	private String dirct4 = "dictionary4.dic";
	
	@Test
	public void test() throws IOException, ParseException {
		
		NSpellChecker checker = null;
		try {
			
			checker = new NSpellChecker(directory, config);
			checker.indexDictionary(new TXTLineSourceReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(dirct)), RowConvertor.defaultValue("\\|")), true);
			checker.indexDictionary(new TXTLineSourceReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(dirct)), RowConvertor.defaultValue("\\|")), true);
			String[] suggestions = checker.suggestSimilar("samsung ssda", 2);
			Assert.assertEquals(suggestions.length, 2);
			checker.indexDictionary(new TXTLineSourceReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(dirct)), RowConvertor.defaultValue("\\|")), true);
			suggestions = checker.suggestSimilar("samsung ssda", 2);
			Assert.assertEquals(suggestions.length, 2);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "samsung ssd");
			suggestions = checker.suggestSimilar("samsang ssd", 2, SuggestMode.SUGGEST_MORE_POPULAR, 0.7f);
			Assert.assertEquals(suggestions.length, 2);
			
			suggestions = checker.suggestSimilar("monitar hdmi", 5, SuggestMode.SUGGEST_MORE_POPULAR, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 4);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "monitor hdmi");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "hdmi moniter");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[2]), "moniter hdmi");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[3]), "HDMI MONITE");
			
			suggestions = checker.suggestSimilar("ipone",  5, SuggestMode.SUGGEST_MORE_POPULAR, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 2);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "iphone");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "ipone");
			
			suggestions = checker.suggestSimilar("fotac geforce gtx 1060 mini", 5, SuggestMode.SUGGEST_MORE_POPULAR, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 1);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "ZOTAC GeForce GTX 1050 Mini");
			
			checker.clearIndex();
			suggestions = checker.suggestSimilar("fotac geforce gtx 1060 mini", 5, SuggestMode.SUGGEST_MORE_POPULAR, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 0);
			
			checker.indexDictionary(new TXTLineSourceReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(dirct2)), RowConvertor.defaultValue("\\|")), true);
			suggestions = checker.suggestSimilar("samsumg", 2);
			Assert.assertEquals(suggestions.length, 2);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "samsung");// filter by integer range filter
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "samsumg");// filter by integer range filter
			
			suggestions = checker.suggestSimilar("samsumg ss", 4); // -1 letter
			Assert.assertEquals(suggestions.length, 3);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "samsung ssd");  // click count +, distance +
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "samsung ssa");  // distance +
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[2]), "samsung ssds"); // click count+
			
			suggestions = checker.suggestSimilar("samsumg ssa", 4); // same letter
			Assert.assertEquals(suggestions.length, 3);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "samsung ssa");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "samsung ssd");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[2]), "samsung ssds");
			
			
			suggestions = checker.suggestSimilar("samsumg ssb", 4);
			Assert.assertEquals(suggestions.length, 3);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "samsung ssd");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "samsung ssa");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[2]), "samsung ssds");
			
			
			suggestions = checker.suggestSimilar("samsumg ssdb", 4);
			Assert.assertEquals(suggestions.length, 3);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "samsung ssd");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "samsung ssds");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[2]), "samsung ssa");
			
			suggestions = checker.suggestSimilar("samsumg ssdsss", 4);
			
			suggestions = checker.suggestSimilar("apple ipone", 5, SuggestMode.SUGGEST_MORE_POPULAR, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 1);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "apple iphone");
			
			suggestions = checker.suggestSimilar("apple iphoone", 5, SuggestMode.SUGGEST_MORE_POPULAR, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 1);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "apple iphone");
			
			suggestions = checker.suggestSimilar("gtx 1050", 5, SuggestMode.SUGGEST_MORE_POPULAR, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 2);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "gtx 1050");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "gtx 1060");
			
			suggestions = checker.suggestSimilar("gta 1050", 5, SuggestMode.SUGGEST_MORE_POPULAR, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 2);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "gtx 1050");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "gtx 1060");
			
			checker.clearIndex();
			checker.indexDictionary(new TXTLineSourceReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(dirct3)), RowConvertor.defaultValue("\\|")), true);
			suggestions = checker.suggestSimilar("samsumg ssd", 5);
			Assert.assertEquals(suggestions.length, 2);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "samsung ssd");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "samsumg ssd");
			
			suggestions = checker.suggestSimilar("samsumg ssd", 5, SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 1);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "samsumg ssd");
			
			suggestions = checker.suggestSimilar("cheery", 5, SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 2);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "cherry");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "CHEERG");
			
			checker.clearIndex();
			checker.indexDictionary(new TXTLineSourceReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(dirct4)), RowConvertor.defaultValue("\\|")), true);
			suggestions = checker.suggestSimilar("samsumg ssd", 5, SuggestMode.SUGGEST_MORE_POPULAR, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 3);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "samsung ssd");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "samsumg ssd");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[2]), "samsung sd");
			
			suggestions = checker.suggestSimilar("zotac geforce gtx 1060 miini", 5, SuggestMode.SUGGEST_MORE_POPULAR, NSpellChecker.DEFAULT_ACCURACY);
			Assert.assertEquals(suggestions.length, 2);
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[0]), "ZOTAC GeForce GTX 1060 Mini");
			Assert.assertEquals(SpellCheckResultSplit.split(suggestions[1]), "ZOTAC GeForce GTX 1050 Mini");

			suggestions = checker.suggestSimilar("ssd Samsung", 5, SuggestMode.SUGGEST_MORE_POPULAR, NSpellChecker.DEFAULT_ACCURACY);
			System.out.print(SpellCheckResultSplit.split(suggestions[0]));

		} finally {
			if(checker != null) {
				checker.close();
				Assert.assertTrue(checker.isClose());
			}
		}
		
	}
}
