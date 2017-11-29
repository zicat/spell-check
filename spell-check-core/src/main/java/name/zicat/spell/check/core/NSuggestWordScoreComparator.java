package name.zicat.spell.check.core;

import java.util.Comparator;

import name.zicat.spell.check.core.model.SuggestWordResult;

public class NSuggestWordScoreComparator implements Comparator<SuggestWordResult> {

	@Override
	public int compare(SuggestWordResult first, SuggestWordResult second) {
		
		if(first.distanceCount == second.distanceCount && first.distanceCount == 0) { //same
			return 0;
		}
		
		if(first.distanceCount == 0 && second.distanceResult.allDifferIsNumerice()) {
			return -1;
		}
		
		if(second.distanceCount == 0 && first.distanceResult.allDifferIsNumerice()) {
			return 1;
		}
		
		int deltaDistanceCount = first.distanceCount - second.distanceCount;
		
		if(deltaDistanceCount == 0) {
			return (int)(second.clickCount - first.clickCount);
		}
		
		if(deltaDistanceCount > 1) {
			return 1;
		}
		
		if(deltaDistanceCount < -1) {
			return -1;
		}
		
		if(deltaDistanceCount == -1 && first.distanceCount >= 1) {
			return -1;
		}
		
		if(deltaDistanceCount == 1 && second.distanceCount >= 1) {
			return 1;
		}
		
		return (int)(second.clickCount - first.clickCount);
	}
}
