package uk.org.brindy.taban;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import uk.org.brindy.taban.internal.TabanImpl;
import uk.org.brindy.taban.mock.MockHttpService;
import uk.org.brindy.taban.mock.MockHttpServletRequest;
import uk.org.brindy.taban.mock.MockHttpServletResponse;
import uk.org.brindy.taban.mock.MockLogService;
import uk.org.brindy.taban.mock.MockPersistence;

public class TabanDeleteHandling {

	private Integer lastErrorCode;
	private ByteArrayOutputStream lastOutputStream;

	@Before
	public void init() {
		lastErrorCode = null;
		lastOutputStream = null;
	}

	@Test
	public void deleteNoneExistingContent() throws Exception {

		// construct the engine
		TabanImpl taban = new TabanImpl();

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
		assertEquals(null, lastErrorCode);
	}

	@Test
	public void deleteWithPrevious() throws Exception {

		// construct the engine
		TabanImpl taban = new TabanImpl();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence() {

			@Override
			public JsonNode delete(String location) {
				ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
				node.put("key", "value");
				return node;
			}

		});

		// construct test case
		MockHttpServletRequest req = new MockHttpServletRequest() {
			@Override
			public String getRequestURI() {
				return "/taban/countries";
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

		};

		execute(taban, req, resp);

		// check result
		assertEquals(null, lastErrorCode);
		assertEquals("{\"key\":\"value\"}", lastOutputStream.toString());
	}

	@Test
	public void deleteDirectory() throws Exception {

		// construct the engine
		TabanImpl taban = new TabanImpl();

		// bind dependencies
		taban.bind(new MockHttpService());
		taban.bindOptional(new MockLogService());
		taban.bind(new MockPersistence());

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

		};

		execute(taban, req, resp);

		// check result
		assertEquals((Integer) HttpServletResponse.SC_BAD_REQUEST,
				lastErrorCode);
	}

	@SuppressWarnings("unchecked")
	private void execute(TabanImpl taban, MockHttpServletRequest req,
			MockHttpServletResponse resp) throws Exception, ServletException,
			IOException {
		Map properties = new HashMap();
		properties.put("taban.alias", "/taban");

		taban.activate(properties);
		taban.doDelete(req, resp);
	}

}
