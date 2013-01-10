import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Sides;

public class SerialIn implements SerialPortEventListener
{
	private static final int TIMEOUT = 2000;
	private InputStream in = null;
    private OutputStream out = null;
    private static byte[] bufferin = new byte[15];							//Buffer que contém as leituras de dados vindos do uC
    private byte[] send = new byte[15];
    private static int available=1, oldavailable=0;
    private static String fileExt;
    private static int fileSize;
    private static int bytesRead;
    private static int DataRead=0;
    private static byte [] fileStream = new byte[15];
    private static byte [] printStream = new byte[15];
    private static long beginTime = 0, elapsedTime = 0;
        
	public static void main(String[] args) throws Exception
	{
		
		SerialIn main = new SerialIn();
			  
		HashMap<String, CommPortIdentifier> portMap = null;				//HashMap com as portas série do tipo
																		//CommPortIdentifier
		SerialPort serialPort = null;
		portMap=main.searchForPorts();
		CommPortIdentifier Port = portMap.get("COM7");
        serialPort = main.connect(Port);
        main.initIOStream(serialPort);
        main.initListener(serialPort);
        
        Thread.sleep(4000);
       
        //-------------------------------------
        System.out.println("Ligação efetuada com sucesso.");
        
        fileStream = new byte[15];
        DataRead = 0;
        
        while(!new String(fileStream, 0, DataRead).equalsIgnoreCase("START_FILE_INFO"))
        	Thread.sleep(1);
            	
        bufferin[0]=0;														//Clears bufferin, to receive ACK
        main.send[0] = 49;
    	Thread.sleep(100);
        main.writeData(main.send);
        
        while(!new String(bufferin, 0, 1).equalsIgnoreCase("1"))   			//Waiting for ACK, to clear DataRead
        	Thread.sleep(1);
        
        
        DataRead=0;															//Ready for new reception
        fileStream = new byte[15];
                
		while(!new String(fileStream, 0, DataRead).matches("^E:(GIF|JPEG|PNG|PDF|TXT|RTF|DOC)-S:[0-9]+E$"))
			Thread.sleep(1);

        String[] fileInfo = new String(fileStream, 0, DataRead).split("-");
        fileExt = fileInfo[0].split(":")[1];      	
        fileSize = Integer.parseInt(fileInfo[1].split(":")[1].substring(0, fileInfo[1].split(":")[1].length() - 1));
		
		System.out.printf("File Info:\nExt: %s\nSize: %d\n", fileExt, fileSize);
    	
		bufferin[0]=0;
        main.send[0] = 49;
	    main.writeData(main.send);
	    
	    while(!new String(bufferin, 0, 1).equalsIgnoreCase("1"))   			//Waiting for ACK, to clear DataRead
	       	Thread.sleep(1);	
	    
	    
	    DataRead=0;
        fileStream = new byte[15];
		       
        while(!new String(fileStream, 0, DataRead).equalsIgnoreCase("END_FILE_INFO"));
                	
        bufferin[0]=0;
        main.send[0] = 49;
        Thread.sleep(10);
        main.writeData(main.send);
        
        while(!new String(bufferin, 0, 1).equalsIgnoreCase("1"))   			//Waiting for ACK, to clear DataRead
	       	Thread.sleep(1);
       
        
        DataRead=0;
        fileStream = new byte[15];   
        
        while(!new String(fileStream, 0, DataRead).equalsIgnoreCase("START_FILE_DATA"));
        
        DataRead=0;
        fileStream = new byte[fileSize + 1];
        Thread.sleep(10);
        main.send[0] = 49;
        main.writeData(main.send);

        beginTime = System.currentTimeMillis();
        while(DataRead < fileSize + 1)
        {
        	elapsedTime = System.currentTimeMillis() - beginTime;
        	if(elapsedTime > ((fileSize * 3)/15) + 100000)
        		System.exit(2);												//Timeout occurred
        }     
        printStream=fileStream;
      
        System.out.println("À espera de END_FILE_DATA...");
        
        
        DataRead=0;
        fileStream = new byte[15];
        
        while(!new String(fileStream, 0, DataRead).equalsIgnoreCase("END_FILE_DATA"));
        
        bufferin[0]=0;
        main.send[0] = 49;
        Thread.sleep(10);
        main.writeData(main.send);
        
        while(!new String(bufferin, 0, 1).equalsIgnoreCase("1"))   			//Waiting for ACK, to clear DataRead
	       	Thread.sleep(1);
        
        main.disconnect(serialPort);
             
                   
        System.out.println(new String(printStream, 0, fileSize));
        

        try {  
        	//locate printer  
        	
        	//FileInputStream is = new FileInputStream("D:/RFID-workspace/rfid-iprint/SerialCom/asd.pdf");
        	InputStream is = new ByteArrayInputStream(printStream,1,fileSize);
        	
    	    if(fileExt.equalsIgnoreCase("PDF"))
    	    {
            	PrintPdf printPDFFile = new PrintPdf(is, "A imprimir PDF...");
        		printPDFFile.print();
        	}
    	    else if(fileExt.equalsIgnoreCase("TXT"))
    	    {
    	    	TextPrint tp = new TextPrint();
    	    	tp.startPrint(is, fileSize);
    	    }
    	    else
        	{
        		PrintService printService = PrintServiceLookup.lookupDefaultPrintService();  
        		System.out.println("Printer online: "+ printService);  

        		//create a print job  
        		DocPrintJob job = printService.createPrintJob();  
        	        	
	        	//define the format of print document  
	        	DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;  
	        	 
	        	PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
	        	
	        	
	        	aset.add(new Copies(1));
	        	aset.add(Sides.ONE_SIDED);
        	
	        	//print the data  
	        	Doc doc = new SimpleDoc(is, flavor, null);  
	        	job.print(doc, aset);  
        	}
        	Thread.sleep(20000);
        	
        	//is.close();  
        	System.out.println("Printing Done!!");  
        	  
        	
        	
        	} catch (Exception e) {  
        	e.printStackTrace();  
        	}  
        
        
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
            commPort = Port.open("COM7", TIMEOUT); 				//A partir de CommPortIdentifier obtém-se CommPort
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
     * Lê um array de bytes de informação sempre que sejam transmitidos para o computador pela porta série
     * e guarda-os na variável global bufferin de tamanho máximo 1024B.
     *  
     */
	public synchronized void serialEvent(SerialPortEvent evt) 
	{
		
		int bytesAvailable = 0;
		bytesRead = 0;
		bufferin = new byte[16];
		if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)	  //Verifica se o evento é transmissão de dados
        {
			try {
				if ((bytesAvailable = in.available()) > 0)
				{
		
					while(bytesRead < bytesAvailable)
						bytesRead = bytesRead + in.read(bufferin, bytesRead, bytesAvailable - bytesRead);
							
					System.arraycopy(bufferin, 0, fileStream, DataRead, bytesRead);
					DataRead += bytesRead;
					
					System.out.printf("%s - %d\n",new String(bufferin, 0, bytesRead), bytesRead);

					System.out.printf("DataRead: %d\n", DataRead);
					

				}
				
			} catch (IOException e) {
				System.out.println("Error receiving data.\n");
			}
        }
	}
	

	
	
	/*
	 * writeData(byte[] writebyte)
	 * 
	 * Envia um array de bytes de dados pela porta série, depois de ter sido efectuada uma ligação com sucesso.
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

