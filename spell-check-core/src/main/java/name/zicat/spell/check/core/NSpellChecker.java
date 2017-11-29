package name.zicat.spell.check.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import name.zicat.spell.check.core.datasource.Record;
import name.zicat.spell.check.core.model.DistanceResult;
import name.zicat.spell.check.core.model.SuggestWordResult;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.payloads.MaxPayloadFunction;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;

import name.zicat.spell.check.core.analyzer.SpellCheckPayloadSimilarity;
import name.zicat.spell.check.core.datasource.DataSourceReader;
import name.zicat.spell.check.core.utils.Longs;
import name.zicat.spell.check.core.utils.Strings;

/**
 * @ThreadSafe
 * @author zicat
 *
 */
public class NSpellChecker implements Closeable {
	
	public static final float DEFAULT_ACCURACY = 0.7f;
	public static final String F_WORD = "word";
	public static final String F_WORD_LENGTH = "word_length";
	public static final String F_NGRAM = "word_gram";
	public static final String F_CLICK_COUNT = "word_click_count";

	private float accuracy = DEFAULT_ACCURACY;

	private volatile DirectoryReader indexReader;
	private volatile IndexSearcher searcher;
	private volatile IndexWriter indexWriter;
	

	private final Object searcherLock = new Object();
	private final Object modifyCurrentIndexLock = new Object();

	private volatile boolean closed = false;

	private NStringDistance sd;
	private Comparator<SuggestWordResult> comparator;
	private final Similarity similarity = new SpellCheckPayloadSimilarity();
	public static final char PAYLOAD_LIMIT = '|';
	public static final String PAYLOAD_LIMIT_PATTERN = "\\|";
	public static final Comparator<SuggestWordResult> DEFAULT_COMPARATOR = new NSuggestWordScoreComparator();
	
	
	/**
	 * 
	 * @param spellIndex
	 * @param config
	 * @param sd
	 * @throws IOException
	 */
	public NSpellChecker(Directory spellIndex, IndexWriterConfig config, NStringDistance sd) throws IOException {
		this(spellIndex, config, sd, DEFAULT_COMPARATOR);
	}
	
	/**
	 * 
	 * @param spellIndex
	 * @param config
	 * @throws IOException
	 */
	public NSpellChecker(Directory spellIndex, IndexWriterConfig config) throws IOException {
		this(spellIndex, config, new NLevensteinDistance(new LevensteinDistance()));
	}
	
	/**
	 * 
	 * @param spellIndex
	 * @param config
	 * @param sd
	 * @param comparator
	 * @throws IOException
	 */
	public NSpellChecker(Directory spellIndex, IndexWriterConfig config, NStringDistance sd, Comparator<SuggestWordResult> comparator) throws IOException {
		
		setStringDistance(sd);
		setComparator(comparator);
		IndexWriter indexWriter = createIndexWriter(spellIndex, config);
		swapSearcher(indexWriter);
	}
	
	/**
	 * 
	 * @param spellIndex
	 * @param config
	 * @return
	 * @throws IOException
	 */
	private IndexWriter createIndexWriter(Directory spellIndex, IndexWriterConfig config) throws IOException {
		IndexWriter indexWriter = new IndexWriter(spellIndex, config.setSimilarity(similarity));
		indexWriter.commit();
		return indexWriter;
	}
	
	/**
	 * 
	 * @param spellIndex
	 * @param config
	 * @return
	 * @throws IOException
	 */
	private IndexWriter createIndexWriter(IndexWriter indexWriter, IndexWriterConfig newConfig) throws IOException {
		
		Directory directory = indexWriter.getDirectory();
		indexWriter.close();
		IndexWriter newIndexWriter = new IndexWriter(directory, newConfig);
		newIndexWriter.commit();
		return newIndexWriter;
	}

	/**
	 * 
	 * @param comparator
	 */
	public void setComparator(Comparator<SuggestWordResult> comparator) {
		this.comparator = comparator;
	}

	/**
	 * 
	 * @param sd
	 */
	public void setStringDistance(NStringDistance sd) {
		this.sd = sd;
	}

	/**
	 * Suggest similar words.
	 * 
	 * <p>
	 * As the Lucene similarity that is used to fetch the most relevant
	 * n-grammed terms is not the same as the edit distance strategy used to
	 * calculate the best matching spell-checked word from the hits that Lucene
	 * found, one usually has to retrieve a couple of numSug's in order to get
	 * the true best match.
	 *
	 * <p>
	 * I.e. if numSug == 1, don't count on that suggestion being the best one.
	 * Thus, you should set this value to <b>at least</b> 5 for a good
	 * suggestion.
	 *
	 * @param word
	 *            the word you want a spell check done on
	 * @param numSug
	 *            the number of suggested words
	 * @throws IOException
	 *             if the underlying index throws an {@link IOException}
	 * @throws AlreadyClosedException
	 *             if the Spellchecker is already closed
	 * @return String[]
	 * @throws ParseException 
	 *
	 * @see #suggestSimilar(String, int, IndexReader, String, SuggestMode,
	 *      float)
	 */
	public String[] suggestSimilar(String word, int numSug) throws IOException, ParseException {
		return this.suggestSimilar(word, numSug, F_WORD, SuggestMode.SUGGEST_ALWAYS, this.accuracy, new WhitespaceTokenizer());
	}

	/**
	 * Suggest similar words.
	 *
	 * <p>
	 * As the Lucene similarity that is used to fetch the most relevant
	 * n-grammed terms is not the same as the edit distance strategy used to
	 * calculate the best matching spell-checked word from the hits that Lucene
	 * found, one usually has to retrieve a couple of numSug's in order to get
	 * the true best match.
	 *
	 * <p>
	 * I.e. if numSug == 1, don't count on that suggestion being the best one.
	 * Thus, you should set this value to <b>at least</b> 5 for a good
	 * suggestion.
	 *
	 * @param word
	 *            the word you want a spell check done on
	 * @param numSug
	 *            the number of suggested words
	 * @param accuracy
	 *            The minimum score a suggestion must have in order to qualify
	 *            for inclusion in the results
	 * @throws IOException
	 *             if the underlying index throws an {@link IOException}
	 * @throws AlreadyClosedException
	 *             if the Spellchecker is already closed
	 * @return String[]
	 * @throws ParseException 
	 *
	 * @see #suggestSimilar(String, int, IndexReader, String, SuggestMode,
	 *      float)
	 */
	public String[] suggestSimilar(String word, int numSug, float accuracy) throws IOException, ParseException {
		return this.suggestSimilar(word, numSug, F_WORD, SuggestMode.SUGGEST_ALWAYS, accuracy, new WhitespaceTokenizer());
	}
	
	public String[] suggestSimilar(String word, int numSug, SuggestMode mode, float accuracy) throws IOException, ParseException {
		return this.suggestSimilar(word, numSug, F_WORD, mode, accuracy, new WhitespaceTokenizer());
	}


	/**
	 * Suggest similar words (optionally restricted to a field of an index).
	 *
	 * <p>
	 * As the Lucene similarity that is used to fetch the most relevant
	 * n-grammed terms is not the same as the edit distance strategy used to
	 * calculate the best matching spell-checked word from the hits that Lucene
	 * found, one usually has to retrieve a couple of numSug's in order to get
	 * the true best match.
	 *
	 * <p>
	 * I.e. if numSug == 1, don't count on that suggestion being the best one.
	 * Thus, you should set this value to <b>at least</b> 5 for a good
	 * suggestion.
	 *
	 * @param word
	 *            the word you want a spell check done on
	 * @param numSug
	 *            the number of suggested words
	 * @param ir
	 *            the indexReader of the user index (can be null see field
	 *            param)
	 * @param field
	 *            the field of the user index: if field is not null, the
	 *            suggested words are restricted to the words present in this
	 *            field.
	 * @param suggestMode
	 *            (NOTE: if indexReader==null and/or field==null, then this is
	 *            overridden with SuggestMode.SUGGEST_ALWAYS)
	 * @param accuracy
	 *            The minimum score a suggestion must have in order to qualify
	 *            for inclusion in the results
	 * @throws IOException
	 *             if the underlying index throws an {@link IOException}
	 * @throws AlreadyClosedException
	 *             if the Spellchecker is already closed
	 * @return String[] the sorted list of the suggest words with these 2
	 *         criteria: first criteria: the edit distance, second criteria
	 *         (only if restricted mode): the popularity of the suggest words in
	 *         the field of the user index
	 * @throws ParseException 
	 * 
	 */
	private String[] suggestSimilar(String word, int numSug, String field, SuggestMode suggestMode, float accuracy, Tokenizer tokenizer) throws IOException, ParseException {
		
		final IndexSearcher indexSearcher = obtainSearcher();
		try {

			final int freq = indexReader.docFreq(new Term(field, word));
			final int goalFreq = suggestMode == SuggestMode.SUGGEST_MORE_POPULAR ? freq : 0;
			if (suggestMode == SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX && freq > 0) {
				return new String[] { word + "_1.0"};
			}
			
			BooleanQuery.Builder queryBuilder = buildQuery(word, accuracy, tokenizer);
			Query query = queryBuilder.build();
			
			int maxHits = numSug < 4? 40: numSug * 10;
			
			TopDocs topDocs = indexSearcher.search(query, maxHits);
			ScoreDoc[] hits = topDocs.scoreDocs;

			int stop = Math.min(hits.length, maxHits);
			List<SuggestWordResult> suggestions = new ArrayList<>(stop);
			for (int i = 0; i < stop; i++) {

				SuggestWordResult sugWord = new SuggestWordResult();
				Document doc = indexSearcher.doc(hits[i].doc);
				sugWord.string = doc.get(field);
				
				DistanceResult distanceResult = sd.getDistance(word, sugWord.string);
				if(distanceResult.getScore() < accuracy) {
					continue;
				}
				sugWord.distanceResult = distanceResult;
				sugWord.clickCount = Longs.valueOf(doc.get(F_CLICK_COUNT), 1L);
				sugWord.distanceCount = Math.abs((int)(sugWord.string.length() * (1 - distanceResult.getScore()) + 0.5)); 
				if (suggestMode == SuggestMode.SUGGEST_MORE_POPULAR) {
					sugWord.freq = indexReader.docFreq(new Term(field, sugWord.string));
					if (goalFreq > sugWord.freq) {
						continue;
					}
				}
				
				suggestions.add(sugWord);
//				if(sugWord.string.equals("GeForce GTX Titan") || sugWord.string.equals("ZOTAC GeForce GTX")) {
//					/** For Test **/
//					Explanation explanation = indexSearcher.explain(query, hits[i].doc);
//					System.out.println("============================");
//					System.out.println(explanation.toString());
//					System.out.println("Word:" + sugWord.string + ", Score:" + hits[i].score + ", Distance:" + score); 
//				}
			}
				
			suggestions.sort(comparator);
			stop = Math.min(suggestions.size(), numSug);
			String[] result = new String[stop];
			for(int i = 0; i < stop; i++) {
				result[i] = suggestions.get(i).string + "_" + suggestions.get(i).distanceResult.getScore();
			}
			return result;
		} finally {
			releaseSearcher(indexSearcher);
		}
	}
	
	/**
	 * 
	 * @param term
	 * @param accuracy
	 * @return
	 */
	private BooleanQuery.Builder buildTermQuery(String term, float accuracy) {
		
		BooleanQuery.Builder query = new BooleanQuery.Builder();
		boolean isEmpty = true;
		
		for (int ng = getMin4Search(term); ng <= getMax(term); ng++) {

			String[] grams = Strings.formGrams(term, ng); 
			if (grams == null || grams.length == 0) {
				continue; 
			}
			
			for (String gram: grams) {
				SpanTermQuery termQuery = new SpanTermQuery(new Term(F_NGRAM, gram.toLowerCase()));
				query.add(termQuery, BooleanClause.Occur.SHOULD);
				PayloadScoreQuery payloadQuery = new PayloadScoreQuery(termQuery, new MaxPayloadFunction(), false);
				query.add(payloadQuery, BooleanClause.Occur.SHOULD);
				isEmpty = false;
			}
		}
		
		if(isEmpty) {
			return null;
		}
		
		return query;
	}
	
	/**
	 * 
	 * @param word
	 * @param accuracy
	 * @return
	 * @throws IOException 
	 */
	private BooleanQuery.Builder buildQuery(String word, float accuracy, Tokenizer tokenizer) throws IOException {
		
		List<String> terms = new ArrayList<>();
		tokenizer.setReader(new StringReader(word));
		try {
			tokenizer.reset();
			CharTermAttribute termAttribute = tokenizer.getAttribute(CharTermAttribute.class);
			while(tokenizer.incrementToken()) {
				String term = new String(termAttribute.buffer(), 0, termAttribute.length());
				terms.add(term);
			}
		} finally {
			tokenizer.close();
		}
		
		BooleanQuery.Builder query = new BooleanQuery.Builder();
		for(String term: terms) {
			
			BooleanQuery.Builder builder = buildTermQuery(term, accuracy);
			if(builder != null) {
				query.add(builder.build(), BooleanClause.Occur.MUST);
			}
		}
		
		addRangeFilter(query, word, accuracy);
		return query;
	}
	
	
	/**
	 * 
	 * @param query
	 * @param word
	 * @param accuracy
	 */
	private static void addRangeFilter(BooleanQuery.Builder query, String word, float accuracy) {
		
		int minLength = (int) (word.length() * accuracy);
		int maxLength = (int) (word.length() * (2 - accuracy));
		if(minLength < maxLength) {
			query.add(NumericRangeQuery.newIntRange(F_WORD_LENGTH, minLength, maxLength, true, true), BooleanClause.Occur.MUST);
		}
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void clearIndex() throws IOException {
		
		synchronized (modifyCurrentIndexLock) {
			ensureOpen();
			IndexWriter indexWriter = createIndexWriter(this.indexWriter, new IndexWriterConfig(this.indexWriter.getAnalyzer()).setOpenMode(OpenMode.CREATE).setSimilarity(similarity));
			swapSearcher(indexWriter);
		}
	}

	/**
	 * Check whether the word exists in the index.
	 * 
	 * @param word
	 *            word to check
	 * @throws IOException
	 *             If there is a low-level I/O error.
	 * @throws AlreadyClosedException
	 *             if the Spellchecker is already closed
	 * @return true if the word exists in the index
	 */
	public boolean exist(String word) throws IOException {
		final IndexSearcher indexSearcher = obtainSearcher();
		try {
			return indexSearcher.getIndexReader().docFreq(new Term(F_WORD, word)) > 0;
		} finally {
			releaseSearcher(indexSearcher);
		}
	}

	/**
	 *
	 * @param sourceReader
	 * @param fullMerge
	 * @throws IOException
	 */
	public final void indexDictionary(DataSourceReader sourceReader, boolean fullMerge) throws IOException {
		indexDictionary(sourceReader, fullMerge, new WhitespaceTokenizer());
	}

	/**
	 *
	 * @param sourceReader
	 * @param fullMerge
	 * @param tokenizer
	 * @throws IOException
	 */
	public final void indexDictionary(DataSourceReader sourceReader, boolean fullMerge, Tokenizer tokenizer) throws IOException {
		synchronized (modifyCurrentIndexLock) {
			
			ensureOpen();
			final IndexWriter writer = this.indexWriter;
			
			IndexSearcher indexSearcher = obtainSearcher();
			IndexReader reader = indexSearcher.getIndexReader();
			final List<TermsEnum> termsEnums = new ArrayList<>();
			if (reader.maxDoc() > 0) {
				for (final LeafReaderContext ctx : reader.leaves()) {
					Terms terms = ctx.reader().terms(F_WORD);
					if (terms != null)
						termsEnums.add(terms.iterator());
				}
			}

			try {
				
				Record record;
				terms: while ((record = sourceReader.next()) != null) {

					String word = record.getKeyword();
					BytesRef bytesRef = new BytesRef(word);
					int len = word.length();
					if (len < 3) {
						continue;
					}

					if (!termsEnums.isEmpty()) {
						for (TermsEnum te : termsEnums) {
							if (te.seekExact(bytesRef)) {
								continue terms;
							}
						}
					}

					Document doc = createDocument(word, record.getClickCount(), tokenizer);
					writer.addDocument(doc);
				}
			} finally {
				releaseSearcher(indexSearcher);
			}
			
			if (fullMerge) {
				writer.commit();
				writer.forceMerge(1);
			}
			swapSearcher(writer);
		}
	}

	/**
	 *
	 * @param text
	 * @param boost
	 * @param tokenizer
	 * @return
	 * @throws IOException
	 */
	private static Document createDocument(String text, long boost, Tokenizer tokenizer) throws IOException {
		
		Document doc = new Document();
		
		Field f = new StringField(F_WORD, text, Field.Store.YES);
		doc.add(f);
		
		Field lengthF = new IntField(F_WORD_LENGTH, text.length(), Field.Store.YES);
		doc.add(lengthF);
		
		Field clickCountF = new LongField(F_CLICK_COUNT, boost, Field.Store.YES);
		doc.add(clickCountF);
		
		addGram(text, doc, boost, tokenizer);
		return doc;
	}

	/**
	 *
	 * @param term
	 * @return
	 */
	private static int getMin4Index(String term) {
		
		int length = getMin(term);
		return length > 2? length - 1: 2;
	}

	/**
	 *
	 * @param term
	 * @return
	 */
	private static int getMin4Search(String term ) {
		return getMin4Index(term);
	}

	/**
	 *
	 * @param term
	 * @return
	 */
	private static int getMin(String term) {
		
		if(term.length() > 10) {
			return 6;
		} 
		if(term.length() > 7) {
			return 5;
		}
		if(term.length() > 5) {
			return 4;
		}
		if(term.length() > 3) {
			return 3;
		}
		return 2;
	}

	/**
	 *
	 * @param term
	 * @return
	 */
	private static int getMax(String term) {
		
		if(term.length() > 10) {
			return 8;
		} 
		if(term.length() > 7) {
			return 7;
		}
		if(term.length() > 5) {
			return 6;
		}
		return term.length();
	}

	/**
	 *
	 * @param term
	 * @param boost
	 * @return
	 */
	private static String termGram(String term, long boost) {
		
		StringBuilder sb = new StringBuilder();
		for (int ng = getMin4Index(term); ng <= getMax(term); ng++) {

			String[] grams = Strings.formGrams(term, ng); 
			if (grams == null || grams.length == 0) {
				continue; 
			}
			
			for(String gram: grams) {
				sb.append(gram);
				sb.append(PAYLOAD_LIMIT);
				sb.append(boost);
				sb.append(Strings.STRING_EMPTY);
			}
		}
		return sb.toString().trim();
	}

	/**
	 *
	 * @param text
	 * @param doc
	 * @param boost
	 * @param tokenizer
	 * @throws IOException
	 */
	private static void addGram(String text, Document doc, long boost, Tokenizer tokenizer) throws IOException {
		
		StringBuilder sb = new StringBuilder();
		tokenizer.setReader(new StringReader(text));
		try {
			tokenizer.reset();
			CharTermAttribute termAttribute = tokenizer.getAttribute(CharTermAttribute.class);
			while(tokenizer.incrementToken()) {
				String term = new String(termAttribute.buffer(), 0, termAttribute.length());
				sb.append(termGram(term, boost));
				sb.append(Strings.STRING_EMPTY);
			}
		} finally {
			tokenizer.close();
		}
		
		FieldType ft = new FieldType(StringField.TYPE_STORED);
		ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		ft.setTokenized(true);
		ft.setStored(true);
		ft.setStoreTermVectorOffsets(true);
		ft.setStoreTermVectorPayloads(true);
		ft.setStoreTermVectorPositions(true);
		ft.setStoreTermVectors(true);
		Field ngramField = new Field(F_NGRAM, sb.toString().trim(), ft);
		doc.add(ngramField);
	}

	/**
	 *
	 * @return
	 */
	private IndexSearcher obtainSearcher() {
		synchronized (searcherLock) {
			ensureOpen();
			searcher.getIndexReader().incRef();
			return searcher;
		}
	}

	/**
	 *
	 * @param aSearcher
	 * @throws IOException
	 */
	private void releaseSearcher(final IndexSearcher aSearcher) throws IOException {
		aSearcher.getIndexReader().decRef();
	}

	/**
	 *
	 */
	private void ensureOpen() {
		if (closed) {
			throw new AlreadyClosedException("Spellchecker has been closed");
		}
	}
	
	public boolean isClose() {
		return closed;
	}
	/**
	 * Close the IndexSearcher used by this SpellChecker
	 * 
	 * @throws IOException
	 *             if the close operation causes an {@link IOException}
	 * @throws AlreadyClosedException
	 *             if the {@link NSpellChecker} is already closed
	 */
	@Override
	public void close() throws IOException {
		synchronized (searcherLock) {
		      ensureOpen();
		      closed = true;
		      try {
		    	  if (searcher != null) {
		    		  searcher.getIndexReader().close();
		    	  }
		      } finally {
		    	  searcher = null;
		    	  indexReader = null;
		    	  indexWriter.commit();
		    	  indexWriter.close();
		      }
		}
	}

	/**
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void swapSearcher(IndexWriter writer) throws IOException {
		
		final IndexSearcher indexSearcher = createSearcherIfChanged(writer);
		if(indexSearcher == this.searcher)
			return;
		
		synchronized (searcherLock) {
			
			if (closed) {
				indexSearcher.getIndexReader().close();
				throw new AlreadyClosedException("Spellchecker has been closed");
			}
			
			IndexSearcher oldSearch = this.searcher;
			
			this.indexReader = (DirectoryReader) indexSearcher.getIndexReader();
			this.searcher = indexSearcher;
			this.searcher.setSimilarity(similarity);
			
			try {
				if(oldSearch != null) {
					oldSearch.getIndexReader().close();
				}
			} finally {
				if(this.indexWriter != writer) {
					IndexWriter old = this.indexWriter;
					this.indexWriter = writer;
					if(old != null && old.isOpen()) {
						old.close();
					}
				}
			}
		}
	}


	/**
	 * 
	 * @param dir
	 * @param writer
	 * @return
	 * @throws IOException
	 */
	private IndexSearcher createSearcherIfChanged(IndexWriter writer) throws IOException {
		
		DirectoryReader oldReader = this.indexReader;
		IndexSearcher searcher = this.searcher;
		if(oldReader == null) {
			return new IndexSearcher(DirectoryReader.open(writer.getDirectory()));
		} else {
			DirectoryReader reader = DirectoryReader.openIfChanged(oldReader, writer, true);
			if(reader == null)
				return searcher;
			else {
				return new IndexSearcher(reader);
			}
		}
	}
}
