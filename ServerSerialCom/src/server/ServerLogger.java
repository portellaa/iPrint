package server;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import shared.Configs;

public class ServerLogger {
	
	private static FileHandler logFile; 
	
	public static void initialize() throws SecurityException, IOException
	{
		Logger logger = Logger.getLogger("");
	    logger.setLevel(Level.INFO);
	    logFile = new FileHandler(Configs.SERVER_LOG_FILE);
	    
	    logFile.setFormatter(new SimpleFormatter());
	    logger.addHandler(logFile);
	}
	
	
}
