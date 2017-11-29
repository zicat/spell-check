package name.zicat.spell.check.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author zicat
 *
 */
public class DistanceResult {
	
	private final float score;
	private Map<Character, Integer> differ;
	
	public DistanceResult(float score) {
		this.score = score;
		this.differ = new HashMap<>();
	}

	/**
	 *
	 * @param cha
	 * @param diffCount
	 */
	public void addDiff(Character cha, Integer diffCount) {
		differ.put(cha, diffCount);
	}

	/**
	 *
	 * @return
	 */
	public float getScore() {
		return score;
	}

	/**
	 *
	 * @return
	 */
	public Map<Character, Integer> getDiffer() {
		return differ;
	}

	/**
	 *
	 * @return
	 */
	public boolean allDifferIsNumerice() {
		
		if(differ.isEmpty())
			return false;
		
		for(Entry<Character, Integer> entry: differ.entrySet()) {
			if(entry.getKey().charValue() < 48 || entry.getKey().charValue() > 57)
				return false;
		}
		return true;
	}
}
