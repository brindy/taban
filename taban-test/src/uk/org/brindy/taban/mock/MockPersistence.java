package uk.org.brindy.taban.mock;

import org.codehaus.jackson.JsonNode;

import uk.org.brindy.taban.Persistence;
import uk.org.brindy.taban.TabanQuery;

public class MockPersistence implements Persistence {

	@Override
	public JsonNode delete(String location) {
		return null;
	}

	@Override
	public JsonNode read(String location, int start, int limit,
			TabanQuery... queries) {
		return null;
	}

	public JsonNode read(String location, TabanQuery... queries) {
		return null;
	}

	public JsonNode write(String location, JsonNode node) {
		return null;
	}

}
