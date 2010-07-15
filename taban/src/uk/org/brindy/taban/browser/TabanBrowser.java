package uk.org.brindy.taban.browser;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import uk.org.brindy.taban.Taban;
import uk.org.brindy.taban.Util;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

@Component(name = "uk.org.brindy.TabanBrowser", properties = { TabanBrowser.PROPERTY_TABAN_BROWSER_ALIAS
		+ "=/tabanbrowser" })
public class TabanBrowser {

	private static final String PROPERTY_TABAN_BROWSER_ALIAS = "taban.browser.alias";

	private HttpService http;

	private LogService log = Util.nullLogService;

	private String browserAlias;

	private Taban taban;

	@Reference(name = "TABAN")
	public void bind(Taban taban) {
		this.taban = taban;
	}

	@Reference(name = "HTTP")
	public void bind(HttpService service) {
		this.http = service;
	}

	public void unbind(HttpService service) {
		this.http = null;
	}

	@Reference(name = "LOG", optional = true, dynamic = true)
	public void bind(LogService service) {
		this.log = service;
	}

	public void unbind(LogService service) {
		this.log = Util.nullLogService;
	}

	@Activate
	@SuppressWarnings( { "unchecked", "serial" })
	public void activate(Map properties) throws Exception {
		browserAlias = (String) properties.get(PROPERTY_TABAN_BROWSER_ALIAS);

		final HttpContext defaultContext = http.createDefaultHttpContext();
		HttpContext context = new HttpContext() {
			public String getMimeType(String name) {
				return defaultContext.getMimeType(name);
			}

			public URL getResource(String name) {
				return defaultContext.getResource(name);
			}

			public boolean handleSecurity(HttpServletRequest request,
					HttpServletResponse response) throws java.io.IOException {

				String path = request.getPathInfo().substring(
						browserAlias.length());

				if (0 == path.length() || "/".equals(path)) {
					if (browserAlias.endsWith("/")) {
						response.sendRedirect(browserAlias + "index.html");
					} else {
						response.sendRedirect(browserAlias + "/index.html");
					}
					return false;
				}

				return defaultContext.handleSecurity(request, response);
			};

		};

		http.registerResources(browserAlias, "/web-content", context);
		System.out.println("TabanBrowser ready");

		log
				.log(LogService.LOG_INFO, "Registered web-content @ "
						+ browserAlias);

		final String aliasServlet = browserAlias + "/alias";
		http.registerServlet(aliasServlet, new HttpServlet() {

			@Override
			protected void doGet(HttpServletRequest req,
					HttpServletResponse resp) throws ServletException,
					IOException {

				resp.setContentType("application/json");
				resp.getWriter().print(
						"{ \"alias\" : \"" + taban.getAlias() + "\" }");
				resp.getWriter().flush();
			}

		}, null, context);

	}

	@Deactivate
	public void deactivate() {
		http.unregister(browserAlias);
		log.log(LogService.LOG_INFO, "Unregistered web-content @ "
				+ browserAlias);
	}


}
