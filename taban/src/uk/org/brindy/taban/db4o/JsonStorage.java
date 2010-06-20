package uk.org.brindy.taban.db4o;

import org.codehaus.jackson.JsonNode;

public class JsonStorage {

	private String parent;

	private String location;

	private JsonNode node;

	public JsonStorage() {
	}

	public JsonStorage(String location, JsonNode node) {

		if (location.endsWith("/")) {
			throw new IllegalArgumentException(
					"location cannot be a directory : " + location);
		}

		int index = location.lastIndexOf("/");
		this.parent = location.substring(0, index + 1);
		this.location = location.substring(index + 1);
		this.node = node;
	}

	public String getLocation() {
		return location;
	}

	public JsonNode getNode() {
		return node;
	}

	public void setNode(JsonNode node) {
		this.node = node;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

}
