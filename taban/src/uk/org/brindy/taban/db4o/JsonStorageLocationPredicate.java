package uk.org.brindy.taban.db4o;

import com.db4o.query.Predicate;

@SuppressWarnings("serial")
class JsonStorageLocationPredicate extends Predicate {
	private final String location;

	JsonStorageLocationPredicate(String location) {
		this.location = location;
	}

	public boolean match(JsonStorage o) {
		if (!o.getParent().startsWith(location)) {
			return false;
		}
		return true;
	}
}