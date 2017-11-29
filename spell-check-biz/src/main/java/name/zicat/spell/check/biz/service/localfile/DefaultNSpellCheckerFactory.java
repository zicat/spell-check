package name.zicat.spell.check.biz.service.localfile;

import java.io.File;
import java.io.IOException;

import name.zicat.spell.check.core.NSpellChecker;
import name.zicat.spell.check.core.analyzer.SpellCheckStoreAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;


/**
 * @ThreadSafe
 * @author zicat
 *
 */
public class DefaultNSpellCheckerFactory implements NSpellCheckerFactory {

	/**
	 *
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	@Override
	public NSpellChecker create(File dir) throws IOException {
		
		IndexWriterConfig config = new IndexWriterConfig(new SpellCheckStoreAnalyzer(NSpellChecker.PAYLOAD_LIMIT));
        Directory directory = new MMapDirectory(dir.toPath());
       	return new NSpellChecker(directory, config);
	}
}
