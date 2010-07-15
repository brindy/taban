package uk.org.brindy.taban;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class Util {

	public static final LogService nullLogService = new LogService() {

		@Override
		public void log(ServiceReference sr, int level, String message,
				Throwable exception) {
		}

		@Override
		public void log(ServiceReference sr, int level, String message) {
		}

		@Override
		public void log(int level, String message, Throwable exception) {
		}

		@Override
		public void log(int level, String message) {
		}
	};

}
