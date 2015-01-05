package edu.iastate.hungnv.util;

/**
 * 
 * @author HUNG
 *
 */
public class Assertion {

	public static void assertTrue(boolean condition) {
		if (! condition)
			Logging.LOGGER.severe("ASSERTION ERROR! Please debug your program.");
	}
	
	public static void assertFalse(boolean condition) {
		assertTrue(! condition);
	}
	
}
