import java.io.IOException;

import arduinocomm.SerialCom;

import server.ServerLogger;
import server.TCPServer;


public class MainProgram {

	private static SerialCom arduinoSender;
	private static TCPServer server;
	
	public static void main(String[] args) {
		
		try {
			ServerLogger.initialize();
			
			arduinoSender = new SerialCom();
			
			//TODO uncoment to work
			if (!arduinoSender.initialize())
				return;
			
			server = new TCPServer(arduinoSender);
			server.run();
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
			    	server.close();
			    	arduinoSender.disconnect(); }
			});
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
