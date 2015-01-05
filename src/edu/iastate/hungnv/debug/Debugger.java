package edu.iastate.hungnv.debug;

import com.caucho.quercus.Location;

/**
 * 
 * @author HUNG
 *
 */
public class Debugger {
	
    private static final String[][] debugLocations 
    	= new String[][]{
    		// Register scripts
//    		{"functions.wp-scripts.php", "140"},
//    		{"class.wp-dependencies.php", "179"},
    	
    		// Print header scripts
//    		{"script-loader.php", "608"},
//    		{"class.wp-scripts.php", "182"},
//    		{"class.wp-dependencies.php", "39"},
//    		{"class.wp-dependencies.php", "78"},
    		
    		// Print footer scripts
//    		{"script-loader.php", "630"},
//    		{"class.wp-scripts.php", "187"},
//    		{"class.wp-dependencies.php", "39"},
//    		{"class.wp-dependencies.php", "41"},
//    		{"class.wp-dependencies.php", "81"},
    		
    		// Register styles
//    		{"functions.wp-styles.php", "146"},
//    		{"class.wp-dependencies.php", "179"},
    		
    		// Print styles
//    		{"functions.wp-styles.php", "39"},
//    		{"class.wp-dependencies.php", "39"},
//    		{"class.wp-dependencies.php", "90"}, 	
    	
    		// Content
//    		{"post-template.php", "166"},
//    		{"plugin.php", "170"},
// 		   	{"formatting.php", "3210"},
//    		{"shortcodes.php", "151"},
//    		{"shortcodes.php", "151"},
//    		{"shortcodes.php", "233"},
    	
//    		{"", ""},
    	};
	
	private int currentDebugLocation = 0;
    
	/**
	 * Static instance of Debugger
	 */
	public static Debugger inst = new Debugger();
    
	/**
	 * Checks a breakpoint
	 */
    public void checkBreakpoint(Location location) {
	    if (currentDebugLocation < debugLocations.length
	    	&& location.getFileName().endsWith(debugLocations[currentDebugLocation][0])
	    	&& location.getLineNumber() == Integer.valueOf(debugLocations[currentDebugLocation][1]))
	    {
	    	System.out.println("Break point #" + currentDebugLocation + ": " + location);
	    	if (currentDebugLocation < 0)
	    		currentDebugLocation++;
	    }
    }

}
