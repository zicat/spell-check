package name.zicat.spell.check.core;

import name.zicat.spell.check.core.model.DistanceResult;

/**
 * @author zicat
 */
public interface NStringDistance {
	
	/**
	 * 
	 * @param inputString Input String
	 * @param suggestionString Suggestion String
	 * @return
	 */
	DistanceResult getDistance(String inputString, String suggestionString);
}
