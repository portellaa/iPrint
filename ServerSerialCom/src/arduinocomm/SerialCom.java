package arduinocomm;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;
import java.util.logging.Logger;

import shared.Configs;

public class SerialCom implements SerialPortEventListener
{
	private final static Logger LOGGER = Logger.getLogger(SerialCom.class.getName());
	
	private InputStream in = null;
    private OutputStream out = null;
    private byte[] bufferin = new byte[16];							//Buffer que contém as leituras de dados vindos do uC
 
    private int available = 0, bytesRead = 0;
    private byte[] send = null;
    
    private SerialPort serialPort = null;
    private CommPortIdentifier Port = null;

	private int oldavailable;
	
    
    public SerialCom()
    {
    	LOGGER.info("Instanciating SerialCom.");
    	
    	String system = System.getProperty("os.name");
    	HashMap<String, CommPortIdentifier> portMap = searchForPorts();
    	
    	LOGGER.info("Running operating system: " + system);
    	
    	if (system.matches("^Windows.*"))
    	{
    		Port = portMap.get(Configs.ARDUINO_WIN_PORT);
    		LOGGER.info("Windows detected.");
    	}
    	else if (system.matches("^Mac.*"))
    	{
    		Port = portMap.get(Configs.ARDUINO_MAC_PORT);
    		LOGGER.info("OS X detected.");
    	}
		
		LOGGER.info("Instanciated SerialCom.");
    }
    
    public boolean tryInitialize()
    {
    	if (Port == null)
		{
			LOGGER.warning("No arduino device connected.");
			return false;
		}
    	
    	return true;
    }
	
	public boolean initialize()
	{
		if (Port == null)
		{
			LOGGER.warning("No arduino device connected.");
			return false;
		}
		
		serialPort = connect(Port);
		initIOStream();
		initListener();
		
		return true;
	}
	
	/*
	 * searchForPorts()
	 * 
	 * Encontra portas série através do método getPortIdentifiers() da classe CommPortIdentifier
	 * e retorna-as num HashMap do tipo <String, CommPortIdentifier>.
	 * 
	 */
	private HashMap<String, CommPortIdentifier> searchForPorts()
    {
		HashMap<String, CommPortIdentifier> portMap = new HashMap<String, CommPortIdentifier>();
        
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements())
        {
            CommPortIdentifier currentPort = ports.nextElement();
            if (currentPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
                portMap.put(currentPort.getName(), currentPort);
        }
		
		return portMap;
    }
	
	/*
	 * connect(CommPortIdentifier Port)
	 * 
	 * Estabelece uma ligação através da porta série do argumento CommPortIdentifier Port.  
	 * 
	 */
	private SerialPort connect(CommPortIdentifier Port)
    {
       
        CommPort commPort = null;
        SerialPort serialPort = null;
        
        try
        {
            commPort = Port.open(Configs.ARDUINO_WIN_PORT, Configs.TIMEOUT); 				//A partir de CommPortIdentifier obtém-se CommPort
            serialPort = (SerialPort)commPort;					//Cast de CommPort para SerialPort
            serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

        }
        catch (PortInUseException e)
        {
        	System.out.println("A porta "  + Port.getName() +  " está a ser utilizada.");
        }
        catch (Exception e)
        {
        	System.err.println("Não é possível abrir " + Port.getName() + ".");
            e.printStackTrace();
        }
        
        return serialPort;
    }
	
	/*
	 * initIOStream(SerialPort serialPort)
	 * 
	 * Inicializa canais de IO nas variáveis globais in e out.
	 * 
	 */
	private void initIOStream()
    {
        try {
            in = serialPort.getInputStream();
            out = serialPort.getOutputStream();
        }
        catch (IOException e) {
        	System.out.println("Não é possível abrir canais de IO.");
        }
    }
	
	
	/* 
	 * initListener(SerialPort serialPort)
	 * 
	 * Inicializa o Event Listener da classe SerialPort, evento que está permanentemente à escuta de
	 * novas transmissões de dados para serem efectuadas. Evita polling.
	 * 
	 */
	private void initListener()
    {
        try
        {
            serialPort.addEventListener(this);					//addEventListener(SerialPortEventListener lsnr)
            serialPort.notifyOnDataAvailable(true);				//SerialPortEventListener é uma interface
        }
        catch (TooManyListenersException e)
        {
        	System.out.println("Não é possível iniciar interrupções.");
        }
    }

    
    /*
     * serialEvent(SerialPortEvent evt) 
     * 
     * Lê um array de bytes de informação sempre que sejam transmitidos para o computador pela porta série
     * e guarda-os na variável global bufferin de tamanho máximo 1024B.
     *  
     */
	public synchronized void serialEvent(SerialPortEvent evt) 
	{
		if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)	  //Verifica se o evento é transmissão de dados
        {
            try
            {
           		available = in.available();
           		bytesRead = in.read(bufferin);
           		
           		System.out.printf("Data: %s - Size: %d\n", new String(bufferin), bytesRead);
           		bytesRead=in.read(bufferin, oldavailable, available);
           		oldavailable=bytesRead;
            		
            }
            catch (Exception e)
            {
            	System.err.println(e.toString());
            }
        }
	}
	
	
	/*
	 * writeData(byte[] writebyte)
	 * 
	 * Envia um array de bytes de dados pela porta série, depois de ter sido efectuada uma ligação com sucesso.
	 * 
	 */
	private void writeData(byte[] writebytes)
    {
        try
        {
            out.write(writebytes);
            out.flush();
        }
        catch (Exception e)
        {
        	System.err.println(e.toString());
        }
    }
	
	
    /*
     * disconnect(SerialPort serialPort)
     * 
     * Fecha a ligação série da porta especificada no argumento. Remove o event listener.
     * 
     */
    public synchronized void disconnect()
    {
        try
        {
            serialPort.removeEventListener();
            serialPort.close();
            in.close();
            out.close();
                        
            System.out.println("Ligação fechada.");
        }
        catch (Exception e)
        {
        	System.out.println("Não é possível fechar a ligação da porta " + serialPort.getName() + ".");
        }
    }
	
	public boolean sendData(byte[] data, String extension, int bytesSize) throws InterruptedException
	{	
		int length = data.length, t = 0;
		extension = extension.toUpperCase();
		
		LOGGER.info("Enter function sendData");
		
		LOGGER.info("Starting sending file info");
		if (!sendCmdsAndWaitForAck(new String("START_FILE_INFO").getBytes()))
			return false;
		Thread.sleep(Configs.DELAY_PACKETS);
		
		LOGGER.info("Sending file info");
		if (!sendCmdsAndWaitForAck(new String("E:" + extension + "-S:" + bytesSize + "E").getBytes()))
			return false;
		Thread.sleep(Configs.DELAY_PACKETS);
		
		LOGGER.info("Finishing sending file info");
		if (!sendCmdsAndWaitForAck("END_FILE_INFO".getBytes()))
			return false;
		Thread.sleep(Configs.DELAY_PACKETS);
		
		LOGGER.info("Starting sending file data");
		if (!sendCmdsAndWaitForAck(new String("START_FILE_DATA").getBytes()))
			return false;
		Thread.sleep(Configs.DELAY_PACKETS);
		
		LOGGER.info("Sending file data");
		send = new byte[Configs.BYTES_SEND_ARDUINO + 1];
		
		LOGGER.info("Bytes to send: " + length);
		
        while(length >= Configs.BYTES_SEND_ARDUINO)
        {
        	send = new byte[Configs.BYTES_SEND_ARDUINO + 1];
        	for(int i = 0; i < Configs.BYTES_SEND_ARDUINO; i++)
        		send[i]=data[i+t];
        	
        	t += Configs.BYTES_SEND_ARDUINO;
        	
        	if (!sendDataAndWaitForAck(send))
        		return false;
        
        	length -= Configs.BYTES_SEND_ARDUINO;
        	
        	LOGGER.info("Bytes sent: " + t + " - bytes remaining: " + length);
        	
        	Thread.sleep(Configs.DELAY_PACKETS);
        	
        	bufferin[0] = 0;
        }
        
        LOGGER.info("Finished first cycle");
        
        LOGGER.info("Bytes sent: " + t + " - bytes remaining: " + length);
      
        send = new byte[Configs.BYTES_SEND_ARDUINO + 1];
        if(length !=0)
        {
        	for(int i = 0; i < length; i++)
        		send[i] = data[i+t];

        	if (!sendDataAndWaitForAck(send))
        		return false;
        }
    	
    	LOGGER.info("Finished second cycle");
    	
    	Thread.sleep(4000);
    	
    	LOGGER.info("Finishing sending file data");
    	
    	if (!sendCmdsAndWaitForAck(new String("END_FILE_DATA").getBytes()))
    		return false; 
    	
    	return true;
	}
	
	private boolean sendDataAndWaitForAck(byte[] dataBytes) throws InterruptedException
	{
		long startTime = 0;
		int resentTimes = 0;
		boolean resend = false;
		
		while(resentTimes < Configs.RESEND_TIMES)
		{
			resend = false;
			startTime = System.currentTimeMillis();
			
			writeData(dataBytes);
			resentTimes++;
			
			LOGGER.info("Sending info for the " + resentTimes + " time(s)");

			while(bufferin[0] != 49)
			{
				if ((System.currentTimeMillis() - startTime) < (Configs.RESEND_TIMEOUT * 1000))
				{
					Thread.sleep(Configs.TIMEOUT_WAIT_ACKS);
				}
				else
				{
					resend = true;
					break;
				}
			}
			bufferin[0] = 0;
			
			if (!resend)
			{
				bufferin[0] = 0;
				break;
			}
		}
		
		return !resend;
	}
	
	private boolean sendCmdsAndWaitForAck(byte[] messageBytes) throws InterruptedException
	{
		long startTime = 0;
		int resentTimes = 0;
		int receivedAcks = 0;
		boolean resend = false;
		
		while(resentTimes < Configs.RESEND_TIMES)
		{
			receivedAcks = 0;
			writeData(messageBytes);
			resentTimes++;
			
			LOGGER.info("Sending info for the " + resentTimes + " time(s)");
			
			while (receivedAcks < 2)
			{
				LOGGER.info("Waiting " + receivedAcks + " acknowledge.");
				
				resend = false;
				startTime = System.currentTimeMillis();
				
				while(bufferin[0] != 49)
				{
					if ((System.currentTimeMillis() - startTime) < (Configs.RESEND_TIMEOUT * 1000))
					{
						Thread.sleep(Configs.TIMEOUT_WAIT_ACKS);
					}
					else
					{
						resend = true;
						break;
					}
				}
				bufferin[0] = 0;
				
				if (!resend)
				{
					receivedAcks++;
					LOGGER.info(receivedAcks + " received");
				}
				else break;
			}
			
			LOGGER.info("For the time " + resentTimes + " received " + receivedAcks + " acks");
			
			if (!resend)
			{
				return true;
			}
		}
		
		return false;
	}
	
}
