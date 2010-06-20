package uk.org.brindy.taban;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import uk.org.brindy.taban.internal.TabanImpl;
import uk.org.brindy.taban.mock.MockHttpService;
import uk.org.brindy.taban.mock.MockHttpServletRequest;
import uk.org.brindy.taban.mock.MockHttpServletResponse;
import uk.org.brindy.taban.mock.MockIDGenerator;
import uk.org.brindy.taban.mock.MockLogService;
import uk.org.brindy.taban.mock.MockPersistence;

public class TabanPutHandling {

	private Integer lastErrorCode;
	private ByteArrayOutputStream lastOutputStream;
	private String lastLocation;
	private JsonNode lastNode;
	private Map<String, String> headers;

	@Before
	public void init() {
		lastErrorCode = null;
		lastLocation = null;
		lastNode = null;
		headers = new HashMap<String, String>();
	}

	@Test
	public void putContentIntoDirectory() throws Exception {

		// construct the engine
		TabanImpl taban = new TabanImpl();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockIDGenerator());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode write(String location, JsonNode node) {

				lastLocation = location;
				lastNode = node;

				return null;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries/";
			}

			@Override
			public ServletInputStream getInputStream() throws IOException {
				final ByteArrayInputStream in = new ByteArrayInputStream(
						"{\"x\": 1}".getBytes());

				return new ServletInputStream() {
					@Override
					public int read() throws IOException {
						return in.read();
					}
				};

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

			@Override
			public void setHeader(String name, String value) {
				headers.put(name, value);
			}

		};

		// execute test case
		execute(taban, req, resp);

		assertEquals(null, lastErrorCode);
		assertNotNull(lastNode);
		assertEquals(lastNode.get("x").getIntValue(), 1);
		assertEquals(headers.get("taban_autoid"), "424709482");
		assertEquals("/countries/424709482", lastLocation);
	}

	@Test
	public void putContent() throws Exception {

		// construct the engine
		TabanImpl taban = new TabanImpl();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockIDGenerator());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode write(String location, JsonNode node) {

				lastLocation = location;
				lastNode = node;

				return null;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries";
			}

			@Override
			public ServletInputStream getInputStream() throws IOException {
				final ByteArrayInputStream in = new ByteArrayInputStream(
						"{\"x\": 1}".getBytes());

				return new ServletInputStream() {
					@Override
					public int read() throws IOException {
						return in.read();
					}
				};

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

			@Override
			public void setHeader(String name, String value) {
				headers.put(name, value);
			}

		};

		execute(taban, req, resp);

		assertNull(lastOutputStream);
		assertEquals(null, lastErrorCode);
		assertNotNull(lastNode);
		assertEquals(lastNode.get("x").getIntValue(), 1);
		assertEquals(headers.get("taban_autoid"), null);
		assertEquals("/countries", lastLocation);
	}

	@Test
	public void putContentWithExisting() throws Exception {

		// construct the engine
		TabanImpl taban = new TabanImpl();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockIDGenerator());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode write(String location, JsonNode node) {

				lastLocation = location;
				lastNode = node;

				ObjectNode existingNode = new ObjectNode(
						JsonNodeFactory.instance);
				existingNode.put("key", "value");
				return existingNode;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries";
			}

			@Override
			public ServletInputStream getInputStream() throws IOException {
				final ByteArrayInputStream in = new ByteArrayInputStream(
						"{\"x\": 1}".getBytes());

				return new ServletInputStream() {
					@Override
					public int read() throws IOException {
						return in.read();
					}
				};

			}

			@Override
			public String getHeader(String name) {
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("taban_previous", "true");
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

			@Override
			public void setHeader(String name, String value) {
				headers.put(name, value);
			}

		};

		execute(taban, req, resp);

		assertNotNull(lastOutputStream);
		assertEquals("{\"key\":\"value\"}", lastOutputStream.toString());
		assertEquals(null, lastErrorCode);
		assertNotNull(lastNode);
		assertEquals(lastNode.get("x").getIntValue(), 1);
		assertEquals(headers.get("taban_autoid"), null);
		assertEquals("/countries", lastLocation);

	}

	@SuppressWarnings("unchecked")
	private void execute(TabanImpl taban, MockHttpServletRequest req,
			MockHttpServletResponse resp) throws Exception, ServletException,
			IOException {
		Map properties = new HashMap();
		properties.put("taban.alias", "/taban");

		taban.activate(properties);
		taban.doPut(req, resp);
	}

}
