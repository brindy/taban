package uk.org.brindy.taban.db4o;

import org.codehaus.jackson.JsonNode;

import uk.org.brindy.taban.TabanQuery;

@SuppressWarnings("serial")
public class JsonStorageLocationQueryPredicate extends
		JsonStorageLocationPredicate {

	private final TabanQuery[] queries;

	JsonStorageLocationQueryPredicate(String location, TabanQuery[] queries) {
		super(location);
		this.queries = queries;
	}

	@Override
	public boolean match(JsonStorage o) {

		if (!super.match(o)) {
			return false;
		}

		JsonNode node = o.getNode();
		for (TabanQuery query : queries) {

			String[] props = query.property.split("\\.");
			if (null == props || props.length == 0) {
				return false;
			}

			for (String prop : props) {
				prop = prop.substring(1, prop.length() - 1);

				node = node.get(prop);
				if (null == node) {
					return false;
				}
			}

			// do the comparison
			if (!compare(node, query)) {
				return false;
			}
		}

		return true;
	}

	private boolean nodeEquals(JsonNode node, TabanQuery query) {
		if (node.isNumber()) {
			if (Double.parseDouble(query.value) != node.getDoubleValue()) {
				return false;
			}
		} else {
			if (!query.value.equals(node.getValueAsText())) {
				return false;
			}
		}
		return true;
	}

	private boolean compare(JsonNode node, TabanQuery query) {
		try {
			if ("=".equals(query.comparison)) {

				if (!nodeEquals(node, query)) {
					return false;
				}

			} else if ("!=".equals(query.comparison)) {

				if (nodeEquals(node, query)) {
					return false;
				}

			} else if (">".equals(query.comparison)) {

				boolean gt = (node.getDoubleValue() > Double
						.parseDouble(query.value));

				if (!gt) {
					return false;
				}

			} else if ("<".equals(query.comparison)) {

				boolean lt = (node.getDoubleValue() < Double
						.parseDouble(query.value));

				System.out.println("*** lt : " + node.getDoubleValue() + " < "
						+ query.value + " ? " + lt);

				if (!lt) {
					return false;
				}

			}

		} catch (Exception ex) {
			return false;
		}

		return true;
	}
}