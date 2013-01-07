package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import shared.ClientStatus;
import arduinocomm.SerialCom;

public class HandleClient extends Thread {
	
	private final static Logger LOGGER = Logger.getLogger(HandleClient.class.getName());

	private Socket clientSocket = null;
	
	private DataInputStream inClient;
	private DataOutputStream outClient;
	
	private SerialCom arduinoSender;
	
	private boolean running = false;
	
	public HandleClient(SerialCom _arduinoSender, Socket client)
	{
		LOGGER.info("Initializing HandleClient to client IP: " + client.getInetAddress().getHostAddress());
		clientSocket = client;
		
		try {
			LOGGER.info("Opening streams");
			
			inClient = new DataInputStream(client.getInputStream());
			outClient = new DataOutputStream(clientSocket.getOutputStream());
			
			LOGGER.info("Opened streams");
			
			arduinoSender = _arduinoSender;
			
		} catch (IOException e) {
			LOGGER.warning("Error openning streams. Cause: " + e.getCause() + ". Message: " + e.getMessage());
		}
		
		LOGGER.info("Initialized HandleClient to client IP: " + client.getInetAddress().getHostAddress());
	}
	
	
	@Override
	public void run() {
		
		LOGGER.info("Started HandleClient to client IP: " + clientSocket.getInetAddress().getHostAddress());
		
		running = true;
		
		byte[] array = null;
		int bytesAvailable = 0, bytesReaded;
		
		int fileSize = 0;
		String fileExt = "";
		
		ClientStatus dataStatus = ClientStatus.INITIAL;
		
		while (running)
		{	
			try {
				bytesReaded = 0;
				if ((bytesAvailable = inClient.available()) > 0)
				{
					LOGGER.info("Running: " + running + ". Bytes available: " + bytesAvailable);

					array = new byte[bytesAvailable];
					while (bytesReaded < bytesAvailable)
						bytesReaded = bytesReaded + inClient.read(array, bytesReaded, bytesAvailable - bytesReaded);
					
					System.out.println("Data Received from client on server: " + new String(array));
					
					switch(dataStatus)
					{
						case INITIAL:
						{
							if (new String(array).equalsIgnoreCase("START_FILE_INFO"))
							{
								LOGGER.info("Received START_FILE_INFO from client");
								
								outClient.write(1);
								outClient.flush();
								
								dataStatus = ClientStatus.INFO;
							}
							break;
						}
						case INFO:
						{
							String[] fileInfo = new String(array).split("-");
							fileExt = fileInfo[0].split(":")[1];
							fileSize = Integer.parseInt(fileInfo[1].split(":")[1]);
							
							outClient.write(1);
							outClient.flush();
							
							dataStatus = ClientStatus.END_INFO;
							break;
						}
						case END_INFO:
						{
							if (new String(array).equalsIgnoreCase("END_FILE_INFO"))
							{
								outClient.write(1);
								outClient.flush();
								
								dataStatus = ClientStatus.DATA;
							}
							break;
						}
						case DATA:
						{
//							if (arduinoSender.sendData(array, fileExt, fileSize))
//							{
								outClient.write(new String("DATA_SENT_OK").getBytes());
//							}
//							else outClient.write(new String("DATA_SENT_ERROR").getBytes());
							
							running = false;
							
							break;
						}
					}
				}
				
//			} catch (InterruptedException e) {
//				LOGGER.warning("Error receiving data. Cause: " + e.getCause() + ". Message: " + e.getMessage());
			} catch (IOException e) {
				LOGGER.warning("Error receiving data. Cause: " + e.getCause() + ". Message: " + e.getMessage());
			}
		}
		
		LOGGER.info("Finished HandleClient to client IP: " + clientSocket.getInetAddress().getHostAddress());
	}
}
