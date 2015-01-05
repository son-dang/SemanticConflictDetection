package edu.iastate.hungnv.util;

import java.io.OutputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 
 * @author HUNG
 *
 */
public class Logging {
	
	/*
	 * Logging properties
	 */
	
	private static final Level LOG_LEVEL = Level.ALL;
	
	private static final String LOG_FILE = "D:/log1.txt";
	
	private static final boolean SHOW_LOCATION = true;
	
	/*
	 * Logging objects
	 */
	
	public static Logger CONSOLE_LOGGER = createConsoleLogger(System.out, LOG_LEVEL);
	
	//public static Logger FILE_LOGGER = createFileLogger(LOG_FILE, LOG_LEVEL);
	
	public static Logger LOGGER = CONSOLE_LOGGER;
	
	/*
	 * Methods
	 */
	
	private static Logger createConsoleLogger(OutputStream outputStream, Level logLevel) {
		Logger logger = Logger.getLogger(Logging.class.getName() + "ConsoleLogger");
		
		logger.addHandler(createConsoleHandler(outputStream));
		logger.setUseParentHandlers(false);
		
		logger.setLevel(logLevel);
		
		return logger;
	}
	
	private static Logger createFileLogger(String logFile, Level logLevel) {
		Logger logger = Logger.getLogger(Logging.class.getName() + "FileLogger");
		
		logger.addHandler(createFileHandler(logFile));
		logger.setUseParentHandlers(false);
		
		logger.setLevel(logLevel);
		
		return logger;
	}
	
	private static class OutputStreamConsoleHandler extends ConsoleHandler {
		
		public OutputStreamConsoleHandler(OutputStream outputStream) {
			super();
			setOutputStream(outputStream);
		}
		
	}
	
	private static Handler createConsoleHandler(OutputStream outputStream) {
		Handler handler = new OutputStreamConsoleHandler(outputStream);
		handler.setLevel(Level.ALL);
		handler.setFormatter(new MyFormatter());
		
		return handler;
	}
	
	private static Handler createFileHandler(String logFile) {
		Handler handler = null;
		
		try {
			handler = new FileHandler(logFile, 10000000, 1, false);
			handler.setLevel(Level.ALL);
			handler.setFormatter(new MyFormatter());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return handler;
	}
	
	private static class MyFormatter extends Formatter {

		@Override
		public String format(LogRecord record) {
			StringBuffer buf = new StringBuffer();
			
			if (SHOW_LOCATION) {
				buf.append(record.getSourceClassName());
				buf.append(' ');
				buf.append(record.getSourceMethodName());
				buf.append(System.getProperty("line.separator"));
			}
			
			buf.append(record.getLevel());
			buf.append(": ");
			buf.append(formatMessage(record));
			buf.append(System.getProperty("line.separator"));

			return buf.toString();
		}
		
	}
		
}
