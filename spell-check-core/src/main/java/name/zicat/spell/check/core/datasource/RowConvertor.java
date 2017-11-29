package name.zicat.spell.check.core.datasource;

/**
 * @author zicat
 */
public interface RowConvertor {

	/**
	 *
	 * @param line
	 * @return
	 */
	Record convert(String line);

	/**
	 *
	 * @param split
	 * @return
	 */
	static RowConvertor defaultValue(String split) {
		
		return line -> {
            String[] lineSplit = line.split(split);
            return new Record(lineSplit[0], Integer.valueOf(lineSplit[1]));
        };
	}
}
