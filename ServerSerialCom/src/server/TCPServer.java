package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import arduinocomm.SerialCom;

import shared.Configs;

public class TCPServer extends Thread {
	
	private final static Logger LOGGER = Logger.getLogger(TCPServer.class.getName());
	
	private boolean serverRunning = false;
	private ServerSocket socket;
	
	private SerialCom arduinoSender;
	
	public TCPServer(SerialCom _arduinoSender)
	{
		LOGGER.info("Initializing TCP Server");
		
		try {
			socket = new ServerSocket(Configs.SERVER_PORT);
			
			arduinoSender = _arduinoSender;
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
				socket.setSoTimeout(Configs.SERVER_SOCKET_TIMEOUT);
				
				Socket clientSocket = socket.accept();
				LOGGER.info("Received connection from " + clientSocket.getInetAddress().getHostAddress());
				HandleClient clientConnHandler = new HandleClient(arduinoSender, clientSocket);
				clientConnHandler.start();
				
			} catch (SocketTimeoutException e) {
				LOGGER.info("Server socket timeout");
			} catch (IOException e) {
				LOGGER.warning("Socket error. Cause: " + e.getCause() + ". Message: " + e.getMessage());
			}
		}
	}
	
	public void close()
	{
		LOGGER.info("Setting server to close");
		serverRunning = false;
		try {
			socket.close();
		} catch (IOException e) {
			LOGGER.warning("Socket close error. Cause: " + e.getCause() + ". Message: " + e.getMessage());
		}
	}
}
