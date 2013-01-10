package shared;

public class Configs {

	public static final String SERVER_LOG_FILE = "../server.log";
	public static final int SERVER_PORT = 6969;
	public static final int SERVER_SOCKET_TIMEOUT = 999999999;
	
	public static final int BYTES_SEND_ARDUINO = 15;
	public static final int TIMEOUT = 2000;
	public static final String ARDUINO_WIN_PORT = "COM7";				//TODO check port
	public static final String ARDUINO_MAC_PORT = "/dev/tty.usbmodem621";		//TODO complete port
	
	public static final int RESEND_TIMES = 3;
	public static final int THREADS_TIMEOUT = 500;		// threads timeout in milisecondsseconds
	public static final int TIMEOUT_WAIT_ACKS = 10;		// timeout in milisecondsseconds
	public static final int RESEND_TIMEOUT = 10;		// resend information timeout in seconds
	
	public static final int DELAY_PACKETS = 10;		// timeout in milisecondsseconds
}
