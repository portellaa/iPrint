package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import shared.Configs;

public class TCPServer extends Thread {
	
	private final static Logger LOGGER = Logger.getLogger(TCPServer.class.getName());
	
	private boolean serverRunning = false;
	private ServerSocket socket;
	
	public TCPServer()
	{
		LOGGER.info("Initializing TCP Server");
		
		try {
			socket = new ServerSocket(Configs.SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.warning("Couldn't bind socket.\nCause: " + e.getCause() + " Message: " + e.getMessage());
		}
		
		LOGGER.info("Initialized TCP Server.");
	}
	
	@Override
	public void run() {
		
		LOGGER.info("Started TCP Server");
		
		serverRunning = true;
		
		while (serverRunning)
		{
			try {
				
				Socket clientSocket = socket.accept();
				LOGGER.info("Received connection from " + clientSocket.getInetAddress().getHostAddress());
				HandleClient clientConnHandler = new HandleClient(clientSocket);
				clientConnHandler.start();
				
			} catch (IOException e) {
				LOGGER.warning("Socket error. Cause: " + e.getCause() + ". Message: " + e.getMessage());
			}
		}
		
	}
}
