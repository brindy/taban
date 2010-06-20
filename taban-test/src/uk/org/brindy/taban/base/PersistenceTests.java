package uk.org.brindy.taban.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.org.brindy.taban.Persistence;
import uk.org.brindy.taban.TabanQuery;

/**
 * Extend this class if you want to implement your own persistence and test it.
 * 
 * @author brindy
 */
public abstract class PersistenceTests {

	protected Persistence persistence;

	public PersistenceTests() {
		super();
	}

	protected abstract Persistence createPersistence() throws Exception;

	@Before
	public void before() throws Exception {
		persistence = createPersistence();
	}

	@After
	public void after() throws Exception {
	}

	@Test
	public void basicReadWriteContent() throws Exception {
		JsonNode node = persistence.read("/country");
		assertNull(node);

		ObjectNode oNode = new ObjectNode(JsonNodeFactory.instance);
		oNode.put("x", 1);
		assertNull(persistence.write("/country", oNode));

		node = persistence.read("/country");
		assertNotNull(node);
		assertEquals(1, node.get("x").getIntValue());

	}

	@Test
	public void basicReadWriteRootDirectory() throws Exception {
		JsonNode node = persistence.read("/");
		assertNull(node);

		ObjectNode oNode = new ObjectNode(JsonNodeFactory.instance);
		oNode.put("x", 1);
		assertNull(persistence.write("/xyz", oNode));

		node = persistence.read("/");
		assertNotNull(node);

		assertTrue(node instanceof ArrayNode);
		ArrayNode array = (ArrayNode) node;
		assertEquals(1, array.size());

		assertEquals("xyz", array.get(0).getTextValue());

		oNode = new ObjectNode(JsonNodeFactory.instance);
		oNode.put("x", 1);
		assertNull(persistence.write("/fgh/abc", oNode));

		node = persistence.read("/");
		assertNotNull(node);

		assertTrue(node instanceof ArrayNode);
		array = (ArrayNode) node;
		assertEquals(2, array.size());

		assertEquals("xyz", array.get(0).getTextValue());
		assertEquals("fgh/", array.get(1).getTextValue());
	}

	@Test
	public void basicReadWriteRootDirectoryWithPrevious() throws Exception {
		JsonNode node = persistence.read("/");
		assertNull(node);

		ObjectNode oNode = new ObjectNode(JsonNodeFactory.instance);
		oNode.put("x", 1);
		assertNull(persistence.write("/xyz", oNode));

		node = persistence.read("/");
		assertNotNull(node);

		assertTrue(node instanceof ArrayNode);
		ArrayNode array = (ArrayNode) node;
		assertEquals(1, array.size());

		assertEquals("xyz", array.get(0).getTextValue());

		ObjectNode oNode2 = new ObjectNode(JsonNodeFactory.instance);
		oNode2.put("x", 2);

		JsonNode previous = persistence.write("/xyz", oNode2);
		assertNotNull(previous);
		assertEquals(1, previous.get("x").getIntValue());

		JsonNode readNode = persistence.read("/xyz");
		assertNotNull(readNode);
		assertEquals(2, readNode.get("x").getIntValue());

	}

	@Test
	public void basicReadWriteSubDirectoryWithPrevious() throws Exception {
		JsonNode node = persistence.read("/countries/");
		assertNull(node);

		ObjectNode oNode = new ObjectNode(JsonNodeFactory.instance);
		oNode.put("x", 1);
		assertNull(persistence.write("/countries/uk", oNode));

		node = persistence.read("/countries/");
		assertNotNull(node);

		assertTrue(node instanceof ArrayNode);
		ArrayNode array = (ArrayNode) node;
		assertEquals(1, array.size());

		assertEquals("uk", array.get(0).getTextValue());

		ObjectNode oNode2 = new ObjectNode(JsonNodeFactory.instance);
		oNode2.put("x", 2);

		JsonNode previous = persistence.write("/countries/uk", oNode2);
		assertNotNull(previous);
		assertEquals(1, previous.get("x").getIntValue());

		JsonNode readNode = persistence.read("/countries/uk");
		assertNotNull(readNode);
		assertEquals(2, readNode.get("x").getIntValue());

	}

	@Test
	public void readWriteDeleteSubDirectoryWithPrevious() throws Exception {
		basicReadWriteSubDirectoryWithPrevious();
		persistence.delete("/countries/uk");
	}

	@Test
	public void readWriteWithQueries() throws Exception {
		ObjectNode chris = new ObjectNode(JsonNodeFactory.instance);
		chris.put("x", 1);
		chris.put("name", "chris");
		persistence.write("/chris", chris);

		ObjectNode simon = new ObjectNode(JsonNodeFactory.instance);
		simon.put("x", 2);
		simon.put("name", "simon");
		persistence.write("/simon", simon);

		TabanQuery query = new TabanQuery("\"x\"", "=", "1");
		ArrayNode node = (ArrayNode) persistence.read("/", query);
		assertNotNull(node);
		assertEquals(1, node.size());
		assertEquals("chris", node.get(0).getTextValue());

		query = new TabanQuery("\"x\"", "=", "2");
		node = (ArrayNode) persistence.read("/", query);
		assertNotNull(node);
		assertEquals(1, node.size());
		assertEquals("simon", node.get(0).getTextValue());

		query = new TabanQuery("\"x\"", ">", "1");
		node = (ArrayNode) persistence.read("/", query);
		assertNotNull(node);
		assertEquals(1, node.size());
		assertEquals("simon", node.get(0).getTextValue());

		query = new TabanQuery("\"x\"", "<", "2");
		node = (ArrayNode) persistence.read("/", query);
		assertNotNull(node);
		assertEquals(1, node.size());
		assertEquals("chris", node.get(0).getTextValue());

		query = new TabanQuery("\"x\"", "!=", "2");
		node = (ArrayNode) persistence.read("/", query);
		assertNotNull(node);
		assertEquals(1, node.size());
		assertEquals("chris", node.get(0).getTextValue());

	}

	@Test
	public void readWriteWithQueriesNestedObject() throws Exception {
		ObjectNode chris = new ObjectNode(JsonNodeFactory.instance);
		chris.put("name", "chris");

		ObjectNode chrischild = new ObjectNode(JsonNodeFactory.instance);
		chrischild.put("y", 50);
		chris.put("x", chrischild);

		persistence.write("/chris", chris);

		ObjectNode simon = new ObjectNode(JsonNodeFactory.instance);
		simon.put("name", "simon");

		ObjectNode simonchild = new ObjectNode(JsonNodeFactory.instance);
		simonchild.put("y", 100);
		simon.put("x", simonchild);

		persistence.write("/simon", simon);

		TabanQuery query = new TabanQuery("\"x\".\"y\"", "=", "50");
		ArrayNode node = (ArrayNode) persistence.read("/", query);
		assertNotNull(node);
		assertEquals(1, node.size());
		assertEquals("chris", node.get(0).getTextValue());

		query = new TabanQuery("\"x\".\"y\"", "=", "100");
		node = (ArrayNode) persistence.read("/", query);
		assertNotNull(node);
		assertEquals(1, node.size());
		assertEquals("simon", node.get(0).getTextValue());

		query = new TabanQuery("\"x\".\"y\"", ">", "50");
		node = (ArrayNode) persistence.read("/", query);
		assertNotNull(node);
		assertEquals(1, node.size());
		assertEquals("simon", node.get(0).getTextValue());

		query = new TabanQuery("\"x\".\"y\"", "<", "100");
		node = (ArrayNode) persistence.read("/", query);
		assertNotNull(node);
		assertEquals(1, node.size());
		assertEquals("chris", node.get(0).getTextValue());

		query = new TabanQuery("\"x\".\"y\"", "!=", "50");
		node = (ArrayNode) persistence.read("/", query);
		assertNotNull(node);
		assertEquals(1, node.size());
		assertEquals("simon", node.get(0).getTextValue());
	}

	@Test
	public void readWriteWithStartLimit() throws Exception {

		String[] names = { "chris", "simon", "john", "ally", "dean" };

		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
			node.put("x", i + 1);
			node.put("name", name);
			persistence.write("/" + name, node);
		}

		ArrayNode array = (ArrayNode) persistence.read("/", 0, 3);
		assertNotNull(array);
		assertEquals(3, array.size());

		array = (ArrayNode) persistence.read("/", 3, 3);
		assertNotNull(array);
		assertEquals(2, array.size());

		array = (ArrayNode) persistence.read("/", 0, -1);
		assertNotNull(array);
		assertEquals(5, array.size());
	}

	@Test
	public void readWriteWithLimitStartQueries() throws Exception {
		String[] names = { "chris", "simon", "john", "ally", "dean" };

		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
			node.put("x", i + 1);
			node.put("name", name);
			persistence.write("/" + name, node);
		}

		ArrayNode array = (ArrayNode) persistence.read("/", 0, 3,
				new TabanQuery("\"x\"", ">", "1"));
		assertNotNull(array);
		assertEquals(3, array.size());

		array = (ArrayNode) persistence.read("/", 3, 3, new TabanQuery("\"x\"",
				">", "1"));
		assertNotNull(array);
		assertEquals(1, array.size());

		array = (ArrayNode) persistence.read("/", 0, -1, new TabanQuery(
				"\"x\"", ">", "1"));
		assertNotNull(array);
		assertEquals(4, array.size());
	}

	@Test
	public void whatHappensIf() throws Exception {
		String[] names = { "chris", "simon", "john", "ally", "dean" };

		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
			node.put("x", i + 1);
			node.put("name", name);
			persistence.write("/" + name, node);
		}

		ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
		node.put("y", 50);
		assertNull(persistence.write("/chris/data1", node));

		node = new ObjectNode(JsonNodeFactory.instance);
		node.put("y", 65);
		assertNull(persistence.write("/chris/data2", node));

		ArrayNode array = (ArrayNode) persistence.read("/", 0, -1);
		assertNotNull(array);
		assertEquals(6, array.size());

	}

}