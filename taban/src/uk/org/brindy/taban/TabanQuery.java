package uk.org.brindy.taban;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabanQuery {

	private static Pattern PATTERN = Pattern
			.compile("((\"\\pL*\"\\.)*\"\\pL*\")[ ]*([<!=>])[ ]*(.*)");

	public final String property;

	public final String comparison;

	public final String value;

	public TabanQuery(String property, String comparison, String value) {
		this.property = property;
		this.comparison = comparison;
		this.value = value;
	}

	@Override
	public String toString() {
		return property + comparison + value;
	}

	public static TabanQuery parse(String header) {
		Matcher m = PATTERN.matcher(header);
		if (!m.matches()) {
			return null;
		}
		return new TabanQuery(m.group(1), m.group(3), m.group(4));
	}

}
