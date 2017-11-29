package name.zicat.spell.check.client.model;

import java.util.List;

/**
 * @author zicat
 */
public class SpellCheckModel {
	
	private List<String> suggestions;
	
	public SpellCheckModel() {
		super();
	}

	public SpellCheckModel(List<String> suggestions) {
		super();
		this.suggestions = suggestions;
	}

	public List<String> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(List<String> suggestions) {
		this.suggestions = suggestions;
	}
}
