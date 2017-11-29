package name.zicat.spell.check.core;

import java.util.Map;
import java.util.Map.Entry;

import name.zicat.spell.check.core.model.DistanceResult;
import org.apache.lucene.search.spell.StringDistance;

import name.zicat.spell.check.core.utils.Strings;

/**
 * 
 * @author zicat
 *
 */
public class NLevensteinDistance implements NStringDistance {
	
	private StringDistance prox;
	
	public NLevensteinDistance(StringDistance prox) {
		this.prox = prox;
	}

	@Override
	public DistanceResult getDistance(String inputString, String suggestionString) {
		
		String ns1 = Strings.normalize(inputString);
		String ns2 = Strings.normalize(suggestionString);
		float score1 = prox.getDistance(ns1, ns2);
		
		String rns1 = Strings.normalize(inputString, true);
		String rns2 = Strings.normalize(suggestionString, true);
		float score2 = prox.getDistance(rns1, rns2);
		
		float resultScore; 
		if(score1 > score2) {
			resultScore = score1;
		} else {
			resultScore = score2;
		}
		return getDistanceResult(inputString, suggestionString, resultScore);
	}
	
	/**
	 * TODO This implement is not rigorous 
	 * @param inputString
	 * @param suggestionString
	 * @param score
	 * @return
	 */
	private DistanceResult getDistanceResult(String inputString, String suggestionString, float score) {
		
		DistanceResult result = new DistanceResult(score);
		Map<Character, Integer> inputCharCount = Strings.charCount(inputString);
		Map<Character, Integer> suggestionCharCount = Strings.charCount(suggestionString);
		for(Entry<Character, Integer> entry: inputCharCount.entrySet()) {
			Integer suggestionCount = suggestionCharCount.get(entry.getKey());
			if(suggestionCount == null) {
				result.addDiff(entry.getKey(), entry.getValue());
			} else {
				Integer diffCount = suggestionCount - entry.getValue();
				if(diffCount != 0) {
					result.addDiff(entry.getKey(), Math.abs(diffCount));
				}
			}
		}
		return result;
	}
}
