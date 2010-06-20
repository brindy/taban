package uk.org.brindy.taban;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import uk.org.brindy.taban.internal.Taban;
import uk.org.brindy.taban.mock.MockHttpService;
import uk.org.brindy.taban.mock.MockHttpServletRequest;
import uk.org.brindy.taban.mock.MockHttpServletResponse;
import uk.org.brindy.taban.mock.MockLogService;
import uk.org.brindy.taban.mock.MockPersistence;

public class TabanGetHandling {

	private Integer lastErrorCode;
	private ByteArrayOutputStream lastOutputStream;

	@Before
	public void init() {
		lastErrorCode = null;
	}

	@Test
	public void getNoneExistingContentBelowRoot() throws Exception {

		// construct the engine
		Taban taban = new Taban();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence());

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries";
			}

		};

		MockHttpServletResponse resp = new MockHttpServletResponse() {

			@Override
			public void sendError(int error) throws IOException {
				sendError(error, null);
			}

			@Override
			public void sendError(int error, String message) throws IOException {
				lastErrorCode = error;
			}

		};

		// execute test case
		execute(taban, req, resp);

		// check result
		assertEquals((Integer) HttpServletResponse.SC_NOT_FOUND, lastErrorCode);
	}

	@Test
	public void getNoneExistingContentAtRoot() throws Exception {

		// construct the engine
		Taban taban = new Taban();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence());

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/";
			}

		};

		MockHttpServletResponse resp = new MockHttpServletResponse() {

			@Override
			public void sendError(int error) throws IOException {
				sendError(error, null);
			}

			@Override
			public void sendError(int error, String message) throws IOException {
				lastErrorCode = error;
			}

		};

		execute(taban, req, resp);

		// check result
		assertEquals((Integer) HttpServletResponse.SC_NOT_FOUND, lastErrorCode);
	}

	@Test
	public void getNoneExistingContentAboveRoot() throws Exception {

		// construct the engine
		Taban taban = new Taban();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence());

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban";
			}

		};

		MockHttpServletResponse resp = new MockHttpServletResponse() {

			@Override
			public void sendError(int error) throws IOException {
				sendError(error, null);
			}

			@Override
			public void sendError(int error, String message) throws IOException {
				lastErrorCode = error;
			}

		};

		execute(taban, req, resp);

		// check result
		assertEquals((Integer) HttpServletResponse.SC_BAD_REQUEST,
				lastErrorCode);
	}

	@Test
	public void getContentAtRoot() throws Exception {

		// construct the engine
		Taban taban = new Taban();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode read(String location, TabanQuery... queries) {
				ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
				node.put("key", "value");
				return node;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/";
			}

		};

		MockHttpServletResponse resp = new MockHttpServletResponse() {

			@Override
			public void sendError(int error) throws IOException {
				sendError(error, null);
			}

			@Override
			public void sendError(int error, String message) throws IOException {
				lastErrorCode = error;
			}

			@Override
			public PrintWriter getWriter() throws IOException {
				lastOutputStream = new ByteArrayOutputStream();
				return new PrintWriter(lastOutputStream);
			}

		};

		execute(taban, req, resp);

		assertEquals(null, lastErrorCode);
		assertEquals("{\"key\":\"value\"}", lastOutputStream.toString());
	}

	@Test
	public void getDirectory() throws Exception {

		// construct the engine
		Taban taban = new Taban();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode read(String location, TabanQuery... queries) {

				ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
				node.put("key", "value");
				return node;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries/";
			}

		};

		MockHttpServletResponse resp = new MockHttpServletResponse() {

			@Override
			public void sendError(int error) throws IOException {
				sendError(error, null);
			}

			@Override
			public void sendError(int error, String message) throws IOException {
				lastErrorCode = error;
			}

			@Override
			public PrintWriter getWriter() throws IOException {
				lastOutputStream = new ByteArrayOutputStream();
				return new PrintWriter(lastOutputStream);
			}

		};

		execute(taban, req, resp);

		assertEquals(null, lastErrorCode);
		assertEquals("{\"key\":\"value\"}", lastOutputStream.toString());
	}

	@Test
	public void getDirectoryWithStart() throws Exception {

		// construct the engine
		Taban taban = new Taban();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode read(String location, int start, int limit,
					TabanQuery... queries) {

				assertEquals(1, start);
				assertEquals(-1, limit);

				ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
				node.put("key", "value");
				return node;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries/";
			}

			@Override
			public String getHeader(String name) {
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("taban_start", "1");
				return headers.get(name);
			}

		};

		MockHttpServletResponse resp = new MockHttpServletResponse() {

			@Override
			public void sendError(int error) throws IOException {
				sendError(error, null);
			}

			@Override
			public void sendError(int error, String message) throws IOException {
				lastErrorCode = error;
			}

			@Override
			public PrintWriter getWriter() throws IOException {
				lastOutputStream = new ByteArrayOutputStream();
				return new PrintWriter(lastOutputStream);
			}

		};

		execute(taban, req, resp);

		assertEquals(null, lastErrorCode);
		assertEquals("{\"key\":\"value\"}", lastOutputStream.toString());
	}

	@Test
	public void getDirectoryWithLimit() throws Exception {

		// construct the engine
		Taban taban = new Taban();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode read(String location, int start, int limit,
					TabanQuery... queries) {

				assertEquals(0, start);
				assertEquals(5, limit);

				ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
				node.put("key", "value");
				return node;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries/";
			}

			@Override
			public String getHeader(String name) {
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("taban_limit", "5");
				return headers.get(name);
			}

		};

		MockHttpServletResponse resp = new MockHttpServletResponse() {

			@Override
			public void sendError(int error) throws IOException {
				sendError(error, null);
			}

			@Override
			public void sendError(int error, String message) throws IOException {
				lastErrorCode = error;
			}

			@Override
			public PrintWriter getWriter() throws IOException {
				lastOutputStream = new ByteArrayOutputStream();
				return new PrintWriter(lastOutputStream);
			}

		};

		execute(taban, req, resp);

		assertEquals(null, lastErrorCode);
		assertEquals("{\"key\":\"value\"}", lastOutputStream.toString());
	}

	@Test
	public void getDirectoryWithQueries() throws Exception {

		// construct the engine
		Taban taban = new Taban();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode read(String location, TabanQuery... queries) {
				assertEquals(2, queries.length);
				ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
				node.put("key", "value");
				return node;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries/";
			}

			@SuppressWarnings("unchecked")
			@Override
			public Enumeration getHeaders(String name) {
				Map<String, List<String>> headers = new HashMap<String, List<String>>();
				headers.put("taban_query", Arrays.asList("\"a\" > 1",
						"\"b\" = \"x\" "));

				List<String> list = headers.get(name);
				if (null != list) {
					return Collections.enumeration(list);
				}

				return null;
			}

		};

		MockHttpServletResponse resp = new MockHttpServletResponse() {

			@Override
			public void sendError(int error) throws IOException {
				sendError(error, null);
			}

			@Override
			public void sendError(int error, String message) throws IOException {
				lastErrorCode = error;
			}

			@Override
			public PrintWriter getWriter() throws IOException {
				lastOutputStream = new ByteArrayOutputStream();
				return new PrintWriter(lastOutputStream);
			}

		};

		execute(taban, req, resp);

		assertEquals(null, lastErrorCode);
		assertEquals("{\"key\":\"value\"}", lastOutputStream.toString());
	}

	@Test
	public void getDirectoryWithQueriesStartAndLimit() throws Exception {

		// construct the engine
		Taban taban = new Taban();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode read(String location, int start, int limit,
					TabanQuery... queries) {
				assertEquals(2, queries.length);
				assertEquals(3, start);
				assertEquals(4, limit);
				ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
				node.put("key", "value");
				return node;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries/";
			}

			@SuppressWarnings("unchecked")
			@Override
			public Enumeration getHeaders(String name) {
				Map<String, List<String>> headers = new HashMap<String, List<String>>();
				headers.put("taban_query", Arrays.asList("\"a\" > 1",
						"\"b\" = \"x\" "));

				List<String> list = headers.get(name);
				if (null != list) {
					return Collections.enumeration(list);
				}

				return null;
			}

			@Override
			public String getHeader(String name) {
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("taban_limit", "4");
				headers.put("taban_start", "3");
				return headers.get(name);
			}

		};

		MockHttpServletResponse resp = new MockHttpServletResponse() {

			@Override
			public void sendError(int error) throws IOException {
				sendError(error, null);
			}

			@Override
			public void sendError(int error, String message) throws IOException {
				lastErrorCode = error;
			}

			@Override
			public PrintWriter getWriter() throws IOException {
				lastOutputStream = new ByteArrayOutputStream();
				return new PrintWriter(lastOutputStream);
			}

		};

		execute(taban, req, resp);

		assertEquals(null, lastErrorCode);
		assertEquals("{\"key\":\"value\"}", lastOutputStream.toString());
	}

	@Test
	public void getDirectoryWithContentOnlyIncluded() throws Exception {

		// construct the engine
		Taban taban = new Taban();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode read(String location, int start, int limit,
					TabanQuery... queries) {

				assertEquals(0, start);
				assertEquals(-1, limit);
				ArrayNode node = new ArrayNode(JsonNodeFactory.instance);
				node.add("a");
				node.add("b");
				node.add("c");
				return node;
			}

			@Override
			public JsonNode read(String location, TabanQuery... queries) {
				ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
				if (location.endsWith("/a")) {
					node.put("x", "hello");
				} else if (location.endsWith("/b")) {
					node.put("y", 1);
				} else if (location.endsWith("/c")) {
					node.put("z", true);
				} else {
					return null;
				}
				return node;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries/";
			}

			public String getHeader(String name) {
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("taban_include", "true");
				return headers.get(name);
			}

		};

		MockHttpServletResponse resp = new MockHttpServletResponse() {

			@Override
			public void sendError(int error) throws IOException {
				sendError(error, null);
			}

			@Override
			public void sendError(int error, String message) throws IOException {
				lastErrorCode = error;
			}

			@Override
			public PrintWriter getWriter() throws IOException {
				lastOutputStream = new ByteArrayOutputStream();
				return new PrintWriter(lastOutputStream);
			}

		};

		execute(taban, req, resp);

		assertEquals(null, lastErrorCode);
		assertEquals(
				"[{\"a\":{\"x\":\"hello\"}},{\"b\":{\"y\":1}},{\"c\":{\"z\":true}}]",
				lastOutputStream.toString());
	}

	@Test
	public void getDirectoryWithContentAndSubFoldersIncluded() throws Exception {

		// construct the engine
		Taban taban = new Taban();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode read(String location, int start, int limit,
					TabanQuery... queries) {

				assertEquals(0, start);
				assertEquals(-1, limit);
				ArrayNode node = new ArrayNode(JsonNodeFactory.instance);
				node.add("employment/");
				node.add("a");
				node.add("b");
				node.add("education/");
				node.add("c");
				node.add("people/");
				return node;
			}

			@Override
			public JsonNode read(String location, TabanQuery... queries) {
				ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
				if (location.endsWith("/a")) {
					node.put("x", "hello");
				} else if (location.endsWith("/b")) {
					node.put("y", 1);
				} else if (location.endsWith("/c")) {
					node.put("z", true);
				} else {
					fail("Requested content @ " + location);
				}
				return node;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries/";
			}

			public String getHeader(String name) {
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("taban_include", "true");
				return headers.get(name);
			}

		};

		MockHttpServletResponse resp = new MockHttpServletResponse() {

			@Override
			public void sendError(int error) throws IOException {
				sendError(error, null);
			}

			@Override
			public void sendError(int error, String message) throws IOException {
				lastErrorCode = error;
			}

			@Override
			public PrintWriter getWriter() throws IOException {
				lastOutputStream = new ByteArrayOutputStream();
				return new PrintWriter(lastOutputStream);
			}

		};

		execute(taban, req, resp);

		assertEquals(null, lastErrorCode);
		assertEquals(
				"[\"employment/\",{\"a\":{\"x\":\"hello\"}},{\"b\":{\"y\":1}},"
						+ "\"education/\",{\"c\":{\"z\":true}},"
						+ "\"people/\"]", lastOutputStream.toString());
	}

	@SuppressWarnings("unchecked")
	private void execute(Taban taban, MockHttpServletRequest req,
			MockHttpServletResponse resp) throws Exception, ServletException,
			IOException {
		Map properties = new HashMap();
		properties.put("taban.alias", "/taban");

		taban.activate(properties);
		taban.doGet(req, resp);
	}

}
