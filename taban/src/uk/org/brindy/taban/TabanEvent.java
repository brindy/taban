package uk.org.brindy.taban;

import java.util.Dictionary;
import java.util.Hashtable;

import org.codehaus.jackson.JsonNode;
import org.osgi.service.event.Event;

public class TabanEvent extends Event {

	public static final String TOPIC_ROOT = TabanEvent.class.getName().replace(
			'.', '/')
			+ "/";

	public enum Type {
		CREATE, UPDATE, DELETE
	};

	public final String location;

	public final JsonNode oldValue;

	public final JsonNode newValue;

	/**
	 * 
	 * @param type
	 *            the type of event
	 * @param location
	 *            the location of the event
	 * @param oldValue
	 *            the old value (or null if not relevant, e.g. create)
	 * @param newValue
	 *            the new value (or null if not relevent, e.g. delete)
	 */
	public TabanEvent(Type type, String location, JsonNode oldValue,
			JsonNode newValue) {
		super(TOPIC_ROOT + type.toString(), toProperties(location));
		this.location = location;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@SuppressWarnings("unchecked")
	static Dictionary toProperties(String location) {
		Dictionary properties = new Hashtable();
		properties.put("taban.event.location", location);
		return properties;
	}

}
