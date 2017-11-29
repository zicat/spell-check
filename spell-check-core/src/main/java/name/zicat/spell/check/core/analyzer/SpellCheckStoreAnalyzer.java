package name.zicat.spell.check.core.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilter;
import org.apache.lucene.analysis.payloads.IntegerEncoder;
import org.apache.lucene.analysis.payloads.PayloadEncoder;

/**
 * 
 * @author zicat
 *
 */
public class SpellCheckStoreAnalyzer extends Analyzer {
	
	private char delimiter;
	private PayloadEncoder encoder = new IntegerEncoder();
	
	public SpellCheckStoreAnalyzer(char delimiter) {
		this.delimiter = delimiter;
	}
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		
		Tokenizer result = new WhitespaceTokenizer();
		TokenFilter lowerCaseFilter = new LowerCaseFilter(result);
		TokenFilter trimFilter = new TrimFilter(lowerCaseFilter);
		TokenFilter payloadTokenFilter = new DelimitedPayloadTokenFilter(trimFilter, delimiter, encoder);
		return new TokenStreamComponents(result, payloadTokenFilter);
	}

}
