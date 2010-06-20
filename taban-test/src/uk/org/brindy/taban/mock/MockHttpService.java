package uk.org.brindy.taban.mock;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public class MockHttpService implements HttpService {

	@Override
	public void unregister(String arg0) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registerServlet(String arg0, Servlet arg1, Dictionary arg2,
			HttpContext arg3) throws ServletException, NamespaceException {
	}

	@Override
	public void registerResources(String arg0, String arg1, HttpContext arg2)
			throws NamespaceException {
	}

	@Override
	public HttpContext createDefaultHttpContext() {
		return new HttpContext() {
			@Override
			public String getMimeType(String arg0) {
				return null;
			}

			@Override
			public URL getResource(String arg0) {
				return null;
			}

			@Override
			public boolean handleSecurity(HttpServletRequest arg0,
					HttpServletResponse arg1) throws IOException {
				return true;
			}
		};
	}

}
