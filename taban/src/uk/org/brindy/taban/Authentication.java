package uk.org.brindy.taban;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;

public interface Authentication {

	/**
	 * Provide an implementation of
	 * {@link HttpContext#handleSecurity(HttpServletRequest, HttpServletResponse)}
	 */
	boolean handleSecurity(HttpServletRequest request,
			HttpServletResponse response) throws IOException;

}
