package name.zicat.spell.check.core.datasource;

import name.zicat.spell.check.core.NSpellChecker;

/**
 * 
 * @author zicat
 *
 */
public class Record {
	
	private final String keyword;
	private final int clickCount;
	
	public Record(String keyword, int clickCount) {
		
		if(keyword == null)
			throw new NullPointerException("keyword is null");
		
		if(clickCount < 0 )
			clickCount = 0;
		
		this.keyword = keyword.replaceAll(NSpellChecker.PAYLOAD_LIMIT_PATTERN, " ");
		this.clickCount = clickCount;
	}

	/**
	 *
	 * @return
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 *
	 * @return
	 */
	public int getClickCount() {
		return clickCount;
	}
}
