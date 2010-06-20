package uk.org.brindy.taban;

public interface IDGenerator {

	/**
	 * Generate an id for a given location. The location will end with a forward
	 * slash.
	 * 
	 * @param location
	 *            the location
	 * @return the generated id
	 */
	String generateID(String location);

}
