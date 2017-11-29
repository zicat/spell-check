package name.zicat.spell.check.core.model;

/**
 * @author zicat
 */
public class SuggestWordResult {
		  
	  /**
	   * Creates a new empty suggestion with null text.
	   */
	  public SuggestWordResult() {}


	  public int freq;

	  /**
	   * the suggested word
	   */
	  public String string;
	  
	  public long clickCount;
	  
	  public int distanceCount;
	  
	  public DistanceResult distanceResult;

}
