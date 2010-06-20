package uk.org.brindy.taban.db4o;

import java.util.concurrent.atomic.AtomicLong;

public class AutoID {

	private AtomicLong id;

	private String location;

	public AutoID(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	public void set(long id) {
		this.id = new AtomicLong(id);
	}

	public long next() {
		return id.incrementAndGet();
	}

}
