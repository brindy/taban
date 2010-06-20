package uk.org.brindy.taban.mock;

import uk.org.brindy.taban.IDGenerator;

public class MockIDGenerator implements IDGenerator {

	@Override
	public String generateID(String location) {
		return String.valueOf(location.hashCode());
	}

}
