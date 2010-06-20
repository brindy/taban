package uk.org.brindy.taban.mock;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class MockLogService implements LogService {

	@Override
	public void log(int level, String message) {
		log(level, message, null);
	}

	@Override
	public void log(int level, String message, Throwable exception) {
		log(null, level, message, exception);
	}

	public void log(ServiceReference sr, int level, String message) {
		log(sr, level, message, null);
	}

	public void log(ServiceReference sr, int level, String message,
			Throwable exception) {
		System.out.println(sr + " : " + level + " : " + message + " : "
				+ exception);
	}

}
