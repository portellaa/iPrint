package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class HandleClient extends Thread {
	
	private final static Logger LOGGER = Logger.getLogger(HandleClient.class.getName());

	private Socket clientSocket = null;
	
	private DataInputStream inClient;
	private DataOutputStream outClient;
	
	private boolean running = false;
	
	public HandleClient(Socket client)
	{
		LOGGER.info("Initializing HandleClient to client IP: " + client.getInetAddress().getHostAddress());
		clientSocket = client;
		
		try {
			inClient = new DataInputStream(client.getInputStream());
			outClient = new DataOutputStream(clientSocket.getOutputStream());
			
			LOGGER.info("Open streams");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOGGER.info("Initialized HandleClient to client IP: " + client.getInetAddress().getHostAddress());
	}
	
	
	@Override
	public void run() {
		
		LOGGER.info("Started HandleClient to client IP: " + clientSocket.getInetAddress().getHostAddress());
		
		running = true;
		
		byte[] array = null;
		int bytesAvailable = 0;
		
		while (running)
		{	
			try {
				if ((bytesAvailable = inClient.available()) > 0)
				{
					LOGGER.info("Running: " + running + ". Bytes available: " + bytesAvailable);

					array = new byte[bytesAvailable];
					inClient.read(array);
					
					System.out.println("Data Received from client on server: " + new String(array));
				}
				
			} catch (IOException e) {
				LOGGER.warning("Error receiving data. Cause: " + e.getCause() + ". Message: " + e.getMessage());
			}
		}
		
	}
}
