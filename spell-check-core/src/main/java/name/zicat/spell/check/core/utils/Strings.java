package name.zicat.spell.check.core.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author zicat
 *
 */
public class Strings {
	
	public static final String PATTERN_EMPTY = "\\s+";
	public static final String STRING_EMPTY = " ";
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static final int termCount(String s) {
		
		if(s == null)
			return 0;
		
		s = s.trim();
		if(s.isEmpty())
			return 0;
		
		String[] sSplit = s.split(PATTERN_EMPTY);
		return sSplit.length;
	}
	
	/**
	 * 
	 * @param text
	 * @param ng
	 * @return
	 */
	public static String[] formGrams(String text, int ng) {
		
 		int len = text.length();
 		
 		if(len < ng) {
 			return null;
 		}
 		String[] res = new String[len - ng + 1];
 		for (int i = 0; i < len - ng + 1; i++) {
 			res[i] = text.substring(i, i + ng);
 		}
 		return res;
 	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static final String normalize(String s, boolean reversed) {
		
		if(s == null || s.isEmpty())
			return s;
		
		s = s.trim().toLowerCase();
		
		String[] sSplit = reversed?reverseSplit(s, PATTERN_EMPTY): split(s, PATTERN_EMPTY);
		if(sSplit == null || sSplit.length <= 1) {
			return s;
		}
		
		Arrays.sort(sSplit);
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < sSplit.length; i ++) {
			sb.append(sSplit[i]);
			if(i != sSplit.length - 1) {
				sb.append(STRING_EMPTY);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static final String normalize(String s) {
		return normalize(s, false);
	}
	
	public static final String[] split(String s, String spit) {
		
		if(s == null || s.isEmpty())
			return null;
		
		return s.split(PATTERN_EMPTY);
	}

	/**
	 *
	 * @param s
	 * @param spit
	 * @return
	 */
	public static final String[] reverseSplit(String s, String spit) {
		
		String[] sSplit = split(s, spit);
		if(sSplit == null || sSplit.length == 0)
			return sSplit;
		
		for(int i = 0; i < sSplit.length; i ++) {
			sSplit[i] = new StringBuffer(sSplit[i]).reverse().toString();
		}
		return sSplit;
	}

	/**
	 *
	 * @param str
	 * @return
	 */
	public static boolean containsNumeric(String str) {
		
		if(str == null || str.isEmpty())
			return false;
		
		for(int i = 0; i < str.length(); i ++) {
			int chr = str.charAt(i);
			if (chr >= 48 && chr <= 57)
               return true;
		}
		return false;
	}

	/**
	 *
	 * @param str
	 * @return
	 */
	public static Map<Character, Integer> charCount(String str) {
		
		if(str == null || str.isEmpty())
			return null;
		
		Map<Character, Integer> result = new HashMap<>();
		for(int i = 0; i < str.length(); i ++) {
			Character c = new Character(str.charAt(i));
			Integer count = result.get(c);
			count = count == null?1: ++count;
			result.put(c, count);
		}
		return result;
	}
}
