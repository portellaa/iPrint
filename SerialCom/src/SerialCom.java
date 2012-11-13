import gnu.io.*;
import java.util.*;
import java.io.*;

public class SerialCom implements SerialPortEventListener
{
	private static final int TIMEOUT = 2000;
	private InputStream in = null;
    private OutputStream out = null;
    private byte[] bufferin = new byte[1024];							//Buffer que contém as leituras de dados vindos do uC
	
	public static void main(String[] args) throws Exception
	{
		File file = new File("D:/RFID-workspace/rfid-iprint/SerialCom/VHDLTutorial.pdf");
		
		FileInputStream fis = new FileInputStream(file);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
                System.out.println("A ler " + readNum + " bytes.");
            }
        } catch (IOException ex) {
            System.out.println("Não é possível converter ficheiro em array de bytes.");
        }
        byte[] readfile = bos.toByteArray();
		
        System.out.println("Ficheiro lido com sucesso.");
        
        
        
		SerialCom main = new SerialCom();
		
		HashMap<String, CommPortIdentifier> portMap = null;				//HashMap com as portas série do tipo
																		//CommPortIdentifier
		SerialPort serialPort = null;
		
		portMap=main.searchForPorts();
		
		CommPortIdentifier Port = portMap.get("COM3");					//Obtenção da porta COM3 a partir do HashMap
		System.out.print("Portas série disponíveis: ");
        System.out.println(Port.getName() + "\n");						//Impressão do nome da porta (COM3)
                
        serialPort = main.connect(Port);
        
        main.initIOStream(serialPort);
        main.initListener(serialPort);

        
        main.writeData(readfile);
        
        
        main.disconnect(serialPort);
        
	}
	
	/*
	 * searchForPorts()
	 * 
	 * Encontra portas série através do método getPortIdentifiers() da classe CommPortIdentifier
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
	 * Estabelece uma ligação através da porta série do argumento CommPortIdentifier Port.  
	 * 
	 */
	public SerialPort connect(CommPortIdentifier Port)
    {
       
        CommPort commPort = null;
        SerialPort serialPort = null;
        
        try
        {
            commPort = Port.open("COM3", TIMEOUT); 				//A partir de CommPortIdentifier obtém-se CommPort
            serialPort = (SerialPort)commPort;					//Cast de CommPort para SerialPort
            serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

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
	public void initIOStream(SerialPort serialPort)
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
	public void initListener(SerialPort serialPort)
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
     * Lê bytes de informação sempre que sejam transmitidos para o computador pela porta série e guarda-os
     * na variável global bufferin de 1KB.
     * IMPORTANTE: Poderá ler um array de bytes inteiro (ver arduino.cc).
     * 
     */
	public synchronized void serialEvent(SerialPortEvent evt) 
	{
		int data;
		
		if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)	  //Verifica se o evento é transmissão de dados
        {
            try
            {
                int i = 0;
                data = in.read();									  //Lê um byte do canal de entrada
                while (data > -1)									  //Se o byte lido for -1, então a leitura chegou
                {													  //ao fim. A variável data é um inteiro entre 0 e
                													  //255 que é depois convertida para byte.
                	if(data != '\n')								  //Envio aleatório de '\n' tem que ser ignorado
                	{
                		bufferin[i] = (byte)data;
                		i++;
                	}
                }
            }
            catch (Exception e)
            {
            	System.out.println("Não é possível ler dados.");
            }
        }
	}
	
	
	/*
	 * writeData(int writebyte)
	 * 
	 * Envia um array de bytes de dados pela porta série, depois de ter sido efectuada uma ligação com sucesso.
	 * NOTA: Poderá em opção enviar byte passado como inteiro no argumento. O byte é retirado dos 8 bits menos 
	 * significativos do argumento inteiro.
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
        	System.out.println("Não é possível enviar dados.");
        }
    }
	
	
    /*
     * disconnect(SerialPort serialPort)
     * 
     * Fecha a ligação série da porta especificada no argumento. Remove o event listener.
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
                        
            System.out.println("Ligação fechada.");
        }
        catch (Exception e)
        {
        	System.out.println("Não é possível fechar a ligação da porta " + serialPort.getName() + ".");
        }
    }

	
	
}
