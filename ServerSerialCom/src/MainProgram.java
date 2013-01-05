import java.io.IOException;

import server.ServerLogger;
import server.TCPServer;


public class MainProgram {

	
	public static void main(String[] args) {
		
		try {
			ServerLogger.initialize();
			
			TCPServer server = new TCPServer();
			server.start();
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
