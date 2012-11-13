import gnu.io.*;
import java.util.*;
import java.io.*;

public class SerialCom implements SerialPortEventListener
{
	private static final int TIMEOUT = 2000;
	private InputStream in = null;
    private OutputStream out = null;
    private byte[] bufferin = new byte[1024];							//Buffer que cont�m as leituras de dados
	
	public static void main(String[] args) throws Exception
	{
		SerialCom main = new SerialCom();
		
		HashMap<String, CommPortIdentifier> portMap = null;				//HashMap com as portas s�rie do tipo
																		//CommPortIdentifier
		SerialPort serialPort = null;
		
		portMap=main.searchForPorts();
		
		CommPortIdentifier Port = portMap.get("COM3");					//Obten��o da porta COM3 a partir do HashMap
		System.out.print("Portas s�rie dispon�veis: ");
        System.out.println(Port.getName() + "\n");						//Impress�o do nome da porta (COM3)

        serialPort = main.connect(Port);
        
        main.initIOStream(serialPort);
        main.initListener(serialPort);
        
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
            commPort = Port.open("COM3", TIMEOUT); 				//A partir de CommPortIdentifier obt�m-se CommPort
            serialPort = (SerialPort)commPort;					//Cast de CommPort para SerialPort
            serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

        }
        catch (PortInUseException e)
        {
        	System.out.println("A porta "  + Port.getName() +  " est� a ser utilizada.\n");
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
        	System.out.println("N�o � poss�vel abrir canais de IO.\n");
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
        	System.out.println("N�o � poss�vel iniciar interrup��es.\n");
        }
    }

    
    /*
     * serialEvent(SerialPortEvent evt) 
     * 
     * L� bytes de informa��o sempre que sejam transmitidos para o computador pela porta s�rie e guarda-os
     * na vari�vel global bufferin de 1KB.
     * IMPORTANTE: Poder� ler um array de bytes inteiro (ver arduino.cc).
     * 
     */
	public synchronized void serialEvent(SerialPortEvent evt) 
	{
		int data;
		
		if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)	  //Verifica se o evento � transmiss�o de dados
        {
            try
            {
                int i = 0;
                data = in.read();									  //L� um byte do canal de entrada
                while (data > -1)									  //Se o byte lido for -1, ent�o a leitura chegou
                {													  //ao fim. A vari�vel data � um inteiro entre 0 e
                													  //255 que � depois convertida para byte.
                	if(data != '\n')								  //Envio aleat�rio de '\n' tem que ser ignorado
                	{
                		bufferin[i] = (byte)data;
                		i++;
                	}
                }
            }
            catch (Exception e)
            {
            	System.out.println("N�o � poss�vel ler dados.\n");
            }
        }
	}
	
	
	/*
	 * writeData(int writebyte)
	 * 
	 * Envia um byte de dados pela porta s�rie, depois de ter sido efectuada uma liga��o com sucesso.
	 * O byte � retirado dos 8 bits menos significativos do argumento inteiro.
	 * IMPORTANTE: Poder� em op��o enviar um array de bytes passado no argumento.
	 * 
	 */
	public void writeData(int writebyte)
    {
        try
        {
            out.write(writebyte);
            out.flush();
        }
        catch (Exception e)
        {
        	System.out.println("N�o � poss�vel enviar dados.\n");
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
                        
            System.out.println("Liga��o fechada.\n");
        }
        catch (Exception e)
        {
        	System.out.println("N�o � poss�vel fechar a liga��o da porta " + serialPort.getName() + ".\n");
        }
    }

	
	
}
