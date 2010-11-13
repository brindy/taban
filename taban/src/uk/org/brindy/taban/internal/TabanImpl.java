package uk.org.brindy.taban.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import uk.org.brindy.taban.Authentication;
import uk.org.brindy.taban.IDGenerator;
import uk.org.brindy.taban.Persistence;
import uk.org.brindy.taban.Taban;
import uk.org.brindy.taban.TabanEvent;
import uk.org.brindy.taban.TabanQuery;
import uk.org.brindy.taban.Util;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

@SuppressWarnings("serial")
@Component(immediate = true, properties = { TabanImpl.PROPERTY_TABAN_ALIAS
		+ "=/taban" }, name = "uk.org.brindy.Taban")
public class TabanImpl extends HttpServlet implements Taban {

	public static final String PROPERTY_TABAN_ALIAS = "taban.alias";

	private final ObjectMapper jsonMapper = new ObjectMapper();

	private LogService log = Util.nullLogService;

	private HttpService http;

	private EventAdmin eventAdmin;

	private Persistence persistence;

	private IDGenerator idGeneration;

	private Authentication authentication;

	private String tabanAlias;

	@Reference(optional = true, dynamic = true, name = "AUTH")
	public void bindOptional(Authentication service) {
		this.authentication = service;
	}

	public void unbindOptional(Authentication servce) {
		this.authentication = null;
	}

	@Reference(optional = true, dynamic = true, name = "EVENT")
	public void bindOptional(EventAdmin service) {
		this.eventAdmin = service;
	}

	public void unbindOptional(EventAdmin service) {
		this.eventAdmin = null;
	}

	@Reference(optional = true, dynamic = true, name = "LOG")
	public void bindOptional(LogService service) {
		this.log = service;
	}

	public void unbindOptional(LogService service) {
		this.log = Util.nullLogService;
	}

	@Reference(name = "HTTP")
	public void bind(HttpService service) {
		this.http = service;
	}

	@Reference(name = "PERSISTENCE")
	public void bind(Persistence service) {
		this.persistence = service;
	}

	@Reference(name = "IDGEN")
	public void bind(IDGenerator service) {
		this.idGeneration = service;
	}

	@Activate
	public void activate(Map<String, String> properties) throws Exception {
		tabanAlias = properties.get(PROPERTY_TABAN_ALIAS);
		if (null == tabanAlias || !tabanAlias.startsWith("/")
				|| tabanAlias.endsWith("/")) {
			throw new RuntimeException(
					"The alias for the Taban service must start, but not end, with a forward slash, e.g. /taban");
		}

		// actual activation
		try {
			http.registerServlet(tabanAlias, this, null, createHttpContext());
			System.out.println("Registered servlet @ " + tabanAlias);
			log.log(LogService.LOG_INFO, "Registered servlet @ " + tabanAlias);
		} catch (Exception e) {
			throw new RuntimeException("Failed to register servlet alias", e);
		}
	}

	@Deactivate
	public void deactivate(Map<String, String> properties) throws Exception {
		http.unregister(tabanAlias);
		log.log(LogService.LOG_INFO, "Unregistered servlet @ " + tabanAlias);
	}

	@Override
	public String getAlias() {
		return tabanAlias;
	}

	private String getHeaderOrParam(HttpServletRequest req, String name) {
		String value = req.getHeader(name);
		if (null == value) {
			value = req.getParameter(name);
		}
		return value;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String location = extractResourceLocation(req);
		if (null == location) {
			log.log(LogService.LOG_INFO, "Bad GET request, invalid location");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		log.log(LogService.LOG_INFO, "GET " + location);
		System.out.println("GET " + location);

		TabanQuery[] queries = extractQueriesFromHeaders(req, resp);
		if (null == queries) {
			// this should always return something, or else the response is
			// configured already
			return;
		}

		String includeHeader = getHeaderOrParam(req, "taban_include");
		String startHeader = getHeaderOrParam(req, "taban_start");
		String limitHeader = getHeaderOrParam(req, "taban_limit");
		System.out.println("include = " + includeHeader + ", start = "
				+ startHeader + ", limitHeader = " + limitHeader);
		JsonNode node = null;
		if (location.endsWith("/")
				&& (null != startHeader || null != limitHeader || null != includeHeader)) {

			int start = 0;
			if (null != startHeader) {
				try {
					if ((start = Integer.parseInt(startHeader)) < 0) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e) {
					log.log(LogService.LOG_INFO,
							"Bad GET request, taban_start = " + startHeader);
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"taban_start header must be a number >= 0");
					return;
				}
			}

			int limit = -1;
			if (null != limitHeader) {
				try {
					if ((limit = Integer.parseInt(limitHeader)) <= 0) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e) {
					log.log(LogService.LOG_INFO,
							"Bad GET request, limitHeader = " + limitHeader);
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"taban_limit header must be a number > 0");
				}
			}

			node = persistence.read(location, start, limit, queries);

			if (null != node && Boolean.parseBoolean(includeHeader)) {
				// include all json content
				ArrayNode array = (ArrayNode) node;
				for (int i = 0; i < array.size(); i++) {
					String valueAtIndex = array.get(i).getValueAsText();

					if (!valueAtIndex.endsWith("/")) {
						ObjectNode value = new ObjectNode(
								JsonNodeFactory.instance);
						value.put(valueAtIndex,
								persistence.read(location + valueAtIndex));
						array.set(i, value);
					}

				}

			}

		} else if (null != startHeader || null != limitHeader
				|| null != includeHeader) {
			log.log(LogService.LOG_INFO,
					"Bad GET request, headers supplied for content request");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"taban_start, taban_limit and taban_include "
							+ "are only supported for directory searches");
		} else {
			node = persistence.read(location, queries);
		}

		if (null == node) {
			log.log(LogService.LOG_INFO, location + " not found");
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		sendNode(resp, node);
	}

	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String location = extractResourceLocation(req);
		if (null == location) {
			log.log(LogService.LOG_INFO, "Bad PUT request, invalid location");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		log.log(LogService.LOG_INFO, "PUT " + location);

		JsonParser parser = jsonMapper.getJsonFactory().createJsonParser(
				req.getInputStream());
		JsonNode node = parser.readValueAsTree();
		System.out.println("PUT " + location + " : " + node);

		if (location.endsWith("/")) {
			String id = idGeneration.generateID(location);
			resp.setHeader("taban_autoid", id);
			location = location + id;
		}

		boolean previous = Boolean
				.parseBoolean(req.getHeader("taban_previous"));
		JsonNode oldNode = persistence.write(location, node);

		fireEvent(oldNode == null ? TabanEvent.Type.CREATE
				: TabanEvent.Type.UPDATE, location, oldNode, node);

		if (previous && null != oldNode) {
			sendNode(resp, oldNode);
		}

	}

	@Override
	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String location = extractResourceLocation(req);
		if (null == location) {
			log.log(LogService.LOG_INFO, "Bad DELETE request, invalid location");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		log.log(LogService.LOG_INFO, "DELETE " + location);

		// is the location valid? i.e. does not end with /
		if (location.endsWith("/")) {
			log.log(LogService.LOG_INFO, "Bad PUT request, delete directory "
					+ location);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Cannot delete JSON directories");
			return;
		}

		boolean previous = Boolean
				.parseBoolean(req.getHeader("taban_previous"));

		JsonNode node = persistence.delete(location);

		fireEvent(TabanEvent.Type.DELETE, location, node, null);

		if (previous && null != node) {
			sendNode(resp, node);
		}

	}

	private void fireEvent(TabanEvent.Type type, String location,
			JsonNode oldValue, JsonNode newValue) {
		final EventAdmin eventAdmin = this.eventAdmin;
		if (null != eventAdmin) {
			final TabanEvent event = new TabanEvent(type, location, oldValue,
					newValue);
			new Thread() {
				public void run() {
					eventAdmin.postEvent(event);
				}
			}.start();
		}
	}

	private HttpContext createHttpContext() {

		final HttpContext ctx = http.createDefaultHttpContext();
		return new HttpContext() {

			@Override
			public boolean handleSecurity(HttpServletRequest request,
					HttpServletResponse response) throws IOException {
				if (null != authentication) {
					return authentication.handleSecurity(request, response);
				}
				return ctx.handleSecurity(request, response);
			}

			@Override
			public URL getResource(String name) {
				return ctx.getResource(name);
			}

			@Override
			public String getMimeType(String name) {
				return ctx.getMimeType(name);
			}

		};

	}

	@SuppressWarnings("unchecked")
	private TabanQuery[] extractQueriesFromHeaders(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		Set<TabanQuery> headers = new HashSet<TabanQuery>();

		Enumeration<String> e = req.getHeaders("taban_query");
		if (null != e) {
			while (e.hasMoreElements()) {
				String header = e.nextElement();
				TabanQuery query = TabanQuery.parse(header);
				if (null == query) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, header
							+ " is not a valid query");
					return null;
				}

				log.log(LogService.LOG_DEBUG, "Found : " + header);
				headers.add(query);
			}
		}

		return headers.toArray(new TabanQuery[headers.size()]);
	}

	private String extractResourceLocation(HttpServletRequest req) {

		String uri = req.getRequestURI();
		int index = uri.indexOf(tabanAlias);
		if (-1 == index) {
			log.log(LogService.LOG_WARNING, tabanAlias + " not found in URI "
					+ uri);
			return null;
		}

		if (index + tabanAlias.length() >= uri.length()) {
			log.log(LogService.LOG_WARNING,
					"Trying to access above root resource " + uri + " vs "
							+ tabanAlias);
			return null;
		}

		return uri.substring(index + tabanAlias.length());
	}

	private void sendNode(HttpServletResponse resp, JsonNode node)
			throws IOException, JsonProcessingException {
		resp.setContentType("text/json");
		JsonGenerator g = jsonMapper.getJsonFactory().createJsonGenerator(
				resp.getWriter());
		jsonMapper.writeTree(g, node);
		g.close();
	}

}
