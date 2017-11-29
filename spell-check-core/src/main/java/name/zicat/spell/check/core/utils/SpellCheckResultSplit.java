package name.zicat.spell.check.core.utils;

/**
 * Created by lz31 on 2017/8/24.
 */
public class SpellCheckResultSplit {

    public static String split(String spellCheckStr) {
        int start = spellCheckStr.lastIndexOf("_");
        return spellCheckStr.substring(0, start);
    }
}
