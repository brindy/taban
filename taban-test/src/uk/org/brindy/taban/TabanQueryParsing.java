package uk.org.brindy.taban;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TabanQueryParsing {

	@Test
	public void validHeaders() {
		TabanQuery query = TabanQuery
				.parse("\"stupidly\".\"long\".\"property\" = \"value\"");
		assertNotNull(query);
		assertEquals("\"stupidly\".\"long\".\"property\"", query.property);
		assertEquals("=", query.comparison);
		assertEquals("\"value\"", query.value);

		query = TabanQuery.parse("\"property\" = 66");
		assertNotNull(query);
		assertEquals("\"property\"", query.property);
		assertEquals("=", query.comparison);
		assertEquals("66", query.value);
	}

	@Test
	public void invalidHeaders() {
		TabanQuery query = TabanQuery
				.parse("\"stupidly\".\"long\". = \"value\"");
		assertNull(query);

		query = TabanQuery.parse("\"property\".name = \"value\"");
		assertNull(query);
	}

}
