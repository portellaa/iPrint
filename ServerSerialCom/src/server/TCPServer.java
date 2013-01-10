package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import shared.Configs;
import arduinocomm.SerialCom;

public class TCPServer {
	
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
	
//	@Override
	public void run() {
		
		LOGGER.info("Started TCP Server");
		
		serverRunning = true;
		
		while (serverRunning)
		{
			try {
				socket.setSoTimeout(Configs.SERVER_SOCKET_TIMEOUT);
				
				Socket clientSocket = socket.accept();
				
				LOGGER.info("Received connection from " + clientSocket.getInetAddress().getHostAddress());
				
				HandleClient clientConnHandler = new HandleClient(this, clientSocket);
				clientConnHandler.run();
				
			} catch (SocketTimeoutException e) {
				LOGGER.info("Server socket timeout");
			} catch (IOException e) {
				LOGGER.warning("Socket error. Cause: " + e.getCause() + ". Message: " + e.getMessage());
				close();
			}
		}
	}
	
	public synchronized void close()
	{
		LOGGER.info("Setting server to close");
		serverRunning = false;
		try {
			socket.close();
		} catch (IOException e) {
			LOGGER.warning("Socket close error. Cause: " + e.getCause() + ". Message: " + e.getMessage());
		}
	}
	
	public synchronized boolean sendDataToArduino(byte[] data, String extension, int bytesSize) throws InterruptedException
	{
		return arduinoSender.sendData(data, extension, bytesSize);
	}
}
