package arduinocomm;
import gnu.io.*;
import java.util.*;
import java.io.*;
import java.nio.*;

public class SerialCom implements SerialPortEventListener
{
	private static final int TIMEOUT = 2000;
	private InputStream in = null;
    private OutputStream out = null;
    private byte[] bufferin = new byte[16];							//Buffer que cont�m as leituras de dados vindos do uC
    private int available=1, oldavailable=0,i;
    private byte[] send = new byte[15];
    
    
    private int bytesRead;
    
	public static void main(String[] args) throws Exception
	{
		SerialCom main = new SerialCom();
		int length=0;
		int t=0;
		
		File file = new File("D:/RFID-workspace/rfid-iprint/SerialCom/VHDLTutorial.txt");
		
		FileInputStream fis = new FileInputStream(file);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                length+=readNum;													//Tamanho do array de bytes do ficheiro
            	bos.write(buf, 0, readNum);
                System.out.println("A ler " + readNum + " bytes...");
            }
        } catch (IOException ex) {
            System.out.println("N�o � poss�vel converter ficheiro em array de bytes.");
        }
        byte[] readfile = bos.toByteArray();
		
        System.out.println("Ficheiro lido com sucesso.");
        
        
        //-------------------------------------

		HashMap<String, CommPortIdentifier> portMap = null;				//HashMap com as portas s�rie do tipo
																		//CommPortIdentifier
		SerialPort serialPort = null;
		portMap=main.searchForPorts();
		CommPortIdentifier Port = portMap.get("COM7");
        serialPort = main.connect(Port);
        main.initIOStream(serialPort);
        main.initListener(serialPort);
        
        Thread.sleep(4000);
       
        //-------------------------------------
        
        
        while(length>=15)
        {
        	for(int i=0;i<15;i++)
        		main.send[i]=readfile[i+t];
        	
        	t+=15;
        	main.writeData(main.send);        	
        
        	length-=15;      
        
        	while(main.bufferin[0] != 49);
        	main.bufferin[0] = 0;
        	
        }
        
      
        /*TODO: Se o ficheiro n�o for m�ltiplo de 15 bytes, o peda�o de c�digo abaixo � executado. O main.send � preenchido
       			com os dados que falta enviar e zeros. Mas os dados n�o s�o enviados!
        */
        if(length !=0)
        {
        	for(int i=0;i<length;i++)
        		main.send[i]=readfile[i+t];
        	
        	for(int i=length;i<15;i++)
        		main.send[i]=0;
     	            	
        	main.writeData(main.send);
        }
        
        while(main.bufferin[0] != 49);
    	main.bufferin[0] = 0;
       
    
        
        main.disconnect(serialPort);


	}
	
	/*
	 * searchForPorts()
	 * 
	 * Encontra portas s�rie atrav�s do m�todo getPortIdentifiers() da classe CommPortIdentifier
	 * e retorna-as num HashMap do tipo <String, CommPortIdentifier>.
	 * 
	 */
	public HashMap<String, CommPortIdentifier> searchForPorts()
    {
		HashMap<String, CommPortIdentifier> portMap = new HashMap();
        
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
	 * Estabelece uma liga��o atrav�s da porta s�rie do argumento CommPortIdentifier Port.  
	 * 
	 */
	public SerialPort connect(CommPortIdentifier Port)
    {
       
        CommPort commPort = null;
        SerialPort serialPort = null;
        
        try
        {
            commPort = Port.open("COM4", TIMEOUT); 				//A partir de CommPortIdentifier obt�m-se CommPort
            serialPort = (SerialPort)commPort;					//Cast de CommPort para SerialPort
            serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

        }
        catch (PortInUseException e)
        {
        	System.out.println("A porta "  + Port.getName() +  " est� a ser utilizada.");
        }
        catch (Exception e)
        {
        	System.err.println("N�o � poss�vel abrir " + Port.getName() + ".");
            e.printStackTrace();
        }
        
        return serialPort;
    }
	
	/*
	 * initIOStream(SerialPort serialPort)
	 * 
	 * Inicializa canais de IO nas vari�veis globais in e out.
	 * 
	 */
	public void initIOStream(SerialPort serialPort)
    {
        try {
            in = serialPort.getInputStream();
            out = serialPort.getOutputStream();
        }
        catch (IOException e) {
        	System.out.println("N�o � poss�vel abrir canais de IO.");
        }
    }
	
	
	/*
	 * initListener(SerialPort serialPort)
	 * 
	 * Inicializa o Event Listener da classe SerialPort, evento que est� permanentemente � escuta de
	 * novas transmiss�es de dados para serem efectuadas. Evita polling.
	 * 
	 */
	public void initListener(SerialPort serialPort)
    {
        try
        {
            serialPort.addEventListener(this);					//addEventListener(SerialPortEventListener lsnr)
            serialPort.notifyOnDataAvailable(true);				//SerialPortEventListener � uma interface
        }
        catch (TooManyListenersException e)
        {
        	System.out.println("N�o � poss�vel iniciar interrup��es.");
        }
    }

    
    /*
     * serialEvent(SerialPortEvent evt) 
     * 
     * L� um array de bytes de informa��o sempre que sejam transmitidos para o computador pela porta s�rie
     * e guarda-os na vari�vel global bufferin de tamanho m�ximo 1024B.
     *  
     */
	public synchronized void serialEvent(SerialPortEvent evt) 
	{
		if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)	  //Verifica se o evento � transmiss�o de dados
        {
            try
            {
           		available = in.available();
           		bytesRead = in.read(bufferin);
           		
           		//System.out.printf("Data: %s - Size: %d\n", new String(bufferin), bytesRead);
           		//bytesRead=in.read(bufferin, oldavailable, available);
           		//oldavailable=bytesRead;
            		
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
	 * Envia um array de bytes de dados pela porta s�rie, depois de ter sido efectuada uma liga��o com sucesso.
	 * 
	 */
	public void writeData(byte[] writebytes)
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
     * Fecha a liga��o s�rie da porta especificada no argumento. Remove o event listener.
     * 
     */
    public synchronized void disconnect(SerialPort serialPort)
    {
        try
        {
            serialPort.removeEventListener();
            serialPort.close();
            in.close();
            out.close();
                        
            System.out.println("Liga��o fechada.");
        }
        catch (Exception e)
        {
        	System.out.println("N�o � poss�vel fechar a liga��o da porta " + serialPort.getName() + ".");
        }
    }

	public byte[] intToByteArray(int length)
	{
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
	       
	    b.putInt(length);
		
		return b.array();
	}
	
}