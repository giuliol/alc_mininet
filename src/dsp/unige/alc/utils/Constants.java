package dsp.unige.alc.utils;

public abstract class Constants {

	public static final int CWLEN = 35;
	public static final int PKTSIZE = 1024;
	public static final int BPF = 38016;
	public static final int FPS = 25;
	public static final int WIDTH = 176;
	public static final int HEIGHT = 144;
	public static final long STREAMER_SLEEPTIME = 2;  // ms between packets of the same word
	public static final int FEEDBACK_PKTSIZE = Integer.SIZE/8*2 + Double.SIZE/8 + Long.SIZE/8;
	public static final int FORWARD_PORT = 5558;
	public static final int BACKWARD_PORT = 5557;
	public static final int DECISOR_WINDOWS_SIZE = 5;
	public static final long MAX_INTERFRAME_TIME = 1000 / FPS;
	public static final int CODEWORD_BUFFER_SIZE = 150;
	public static final String RECEIVED_JPS_FILENAME = "RECEIVED.jps";  
	public static final String REFERENCE_JPS_FILENAME = "REFERENCE.jps";
	public static final String TRANSMITTER_LOGFILE = "tx_logfile.log";  
	public static final String RECEIVER_LOGFILE = "rx_logfile.log";
	public static final long UDP_SLEEPTIME = 500;
	public static final String TERMINATION_ACK = "TERMINATION_ACK";
	public static final int TERMINATION_SOCKET_PORT = 5550;
	public static final String TERMINATION_MSG = "DIE!";
	public static final int IMAGE_BUFFER_SIZE = 1000;  



	public static class LOG{
		public static int Silent = 0;
		public static int Verbose = 1;
		public static int Debug = 2;
	}



	public static boolean HARD_LOG;

}
