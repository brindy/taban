package uk.org.brindy.taban;

import org.codehaus.jackson.JsonNode;

public interface Persistence {

	/**
	 * Store some JSON at the given location.
	 * 
	 * @param location
	 *            the location
	 * @param node
	 *            the json
	 * 
	 * @return the previous json value if there was one, otherwise null
	 * 
	 */
	JsonNode write(String location, JsonNode node);

	/**
	 * Read from the given location applying queries, if any.
	 * 
	 * @param location
	 *            the location
	 * @param queries
	 *            the queries
	 * @return the json, or null if there is none
	 */
	JsonNode read(String location, TabanQuery... queries);

	/**
	 * Read from the given location applying queries, if any.
	 * 
	 * @param location
	 *            the location
	 * @param start
	 *            the starting position in the results
	 * @param limit
	 *            the maximum number of results to return, or -1 for all
	 * @param queries
	 *            the queries
	 * @return the json, or null if there is none
	 */
	JsonNode read(String location, int start, int limit, TabanQuery... queries);

	/**
	 * Delete the json at the given location.
	 * 
	 * @param location
	 *            the location, which will not end with a forward slash
	 * 
	 * @return the previous json value if there was one, otherwise null
	 */
	JsonNode delete(String location);

}
