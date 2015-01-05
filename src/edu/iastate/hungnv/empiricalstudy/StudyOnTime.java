package edu.iastate.hungnv.empiricalstudy;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.caucho.quercus.env.Env;

import edu.iastate.hungnv.util.FileIO;
import edu.iastate.hungnv.util.Logging;

/**
 * 
 * @author HUNG
 *
 */
public class StudyOnTime {

	public static final String timeTxtFile = "C:\\Users\\HUNG\\Desktop\\Varex Evaluation\\eval-time.txt";
	
	private long startTime;
	
	/**
	 * Called when Env is started.
	 */
    public void envStarted() {
    	startTime = System.currentTimeMillis();
    }
    
    /**
     * Called when Env is closed.
     */
    public void envClosed(Env env) {
    	long endTime = System.currentTimeMillis();
    	String timeStamp = new SimpleDateFormat("MM-dd-YYYY HH:mm:ss").format(Calendar.getInstance().getTime());
    	
    	String newLine = "Timestamp: " + timeStamp + ". Elapsed time (ms): " + (endTime - startTime);
    	Logging.LOGGER.info(newLine);
    	
    	String fileContent = new File(timeTxtFile).exists() ? FileIO.readStringFromFile(timeTxtFile) : "";
    	FileIO.writeStringToFile(newLine + System.lineSeparator() + fileContent, timeTxtFile);
    }
    
}
