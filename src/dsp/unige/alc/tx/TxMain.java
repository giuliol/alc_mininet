package dsp.unige.alc.tx;

import java.io.Writer;
import java.net.InetAddress;

import dsp.unige.alc.utils.Camera;
import dsp.unige.alc.utils.CodeWord;
import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Constants.LOG;
import dsp.unige.alc.utils.DummyCamera;
import dsp.unige.alc.utils.JpegEncoder;
import dsp.unige.alc.utils.Log;
import dsp.unige.alc.utils.Packet;
import dsp.unige.alc.utils.PacketBuffer;
import dsp.unige.alc.utils.RQEncoder;
import dsp.unige.alc.utils.Visualizer;

public class TxMain {

	private static final int CWLEN = Constants.CWLEN;
	private static final int PKTSIZE = Packet.PKTSIZE;
	private static final int CWBSIZE = Constants.CODEWORD_BUFFER_SIZE;

	private RQEncoder rqEnc;
	private Camera cam;
	private boolean RUNNING;
	private PacketBuffer pBuffer;
	private CodeWordBuffer cwBuffer;
	private Decisor decisor;
	private boolean ADAPTIVE;
	private InetAddress destination;
	private int forwardPort;
	private int backwardPort;
	private int LOG_LEVEL = 0;
	private SessionParameters sessionParameters;
	private Visualizer visualizer;
	private Writer logWriter;
	private int FEC;
	private int Q;
	
	public void setADAPTIVE(boolean aDAPTIVE) {
		ADAPTIVE = aDAPTIVE;
	}

	public void setDestination(InetAddress destination) {
		this.destination = destination;
	}
	public void setForwardPort(int destinationPort) {
		this.forwardPort = destinationPort;
	}

	public void setLogLevel(int level){
		LOG_LEVEL = level;
	}

	public void setBackwardPort(int listeningPort) {
		this.backwardPort = listeningPort;
	}

	public void init(String videoFile){

		int bpf = Constants.BPF;
		int fps = Constants.FPS;

		cam = new DummyCamera();
		((DummyCamera)cam).init(videoFile,bpf,fps);
		((DummyCamera)cam).setLogWriter(logWriter);
		
		sessionParameters = new SessionParameters();
		sessionParameters.setQ(50);
		sessionParameters.setFEC(1);
		
		Q = 50;
		FEC = 1;

		pBuffer = new PacketBuffer();
		try {
			pBuffer.init(CWLEN);
		} catch (Exception e) {
			stopRunning();
			e.printStackTrace();
		}
		cwBuffer = new CodeWordBuffer();
		cwBuffer.init(CWBSIZE);
		cwBuffer.setLogWriter(logWriter);
		RUNNING = true;

		decisor = new Decisor();

	}
	

	public void setVisualizer(Visualizer visualizer) {
		this.visualizer = visualizer;
	}

	public void go() throws Exception{

		cam.open();

		byte [] rawFrame;
		byte [] compressedFrame;
		byte [][] packetsBytes;
		CodeWord word;
		int contentId = 0;
		int codeWordNumber = 0;

		StreamerThread st = new StreamerThread(cwBuffer);
		st.setDestination(destination, forwardPort);
		st.setLogWriter(logWriter);
		st.start();

		FeedbackListenerThread lt = new FeedbackListenerThread(cwBuffer,sessionParameters);
		lt.setDecisor(decisor);
		lt.setBackwardPort(backwardPort);
		lt.setLogWriter(logWriter);
		lt.setADAPTIVE(ADAPTIVE);
		lt.start();
		
		if(checkAll())
			while( cam.hasFrame() && isRunning()){
				rawFrame = cam.getFrame();

				contentId++;
				
				compressedFrame = JpegEncoder.Compress(rawFrame, Q ,Constants.WIDTH, Constants.HEIGHT);
				visualizeFrame(JpegEncoder.Compress(rawFrame, 100 ,Constants.WIDTH, Constants.HEIGHT),contentId);
				if(pBuffer.hasBytesAvailable(compressedFrame.length))		
					// keep filling the packet buffer
				{ 
					pBuffer.put(Packet.fromByteArray(compressedFrame, contentId ));
//					framesInCodeword++;
				}
				else		
					// buffer full, encode and handle to codeword buffer
				{
					rqEnc = new RQEncoder();
					rqEnc.init((CWLEN - FEC) * PKTSIZE, PKTSIZE);
					packetsBytes = rqEnc.encode(pBuffer.getData(),FEC);
					pBuffer.fillWithEncoded(packetsBytes);
					word = CodeWord.fromPacketArray(pBuffer.getPackets(), FEC, codeWordNumber++);
					
//					System.out.println("TxMain.go() word "+word.codeWordNumber+", padding: "+(Constants.CWLEN - sessionParameters.getFEC() - pBuffer.occupancy()));
					
					FEC = sessionParameters.getFEC();
					Q = sessionParameters.getQ();
					Log.i(logWriter,"TxMain","new parameters: FEC="+FEC+", Q="+Q);
					
					if(cwBuffer.getOverflowDanger() && ADAPTIVE){
						
						Q = (int)0.7*Q;
					}
					
					cwBuffer.put(word);
					pBuffer.reset();
					pBuffer.setFec(FEC);
					pBuffer.put(Packet.fromByteArray(compressedFrame, contentId ));

				}
			}
		
		Log.i(logWriter,"main","done, stopping - last content id sent = "+contentId);
		lt.stopRunning();
		st.stopRunning();
		lt.join();
		st.join();
		cleanUp();
	}

	private boolean checkAll() // checks if everything is set
	{
		boolean isOK = true;

		isOK = isOK && (visualizer != null);
		isOK = isOK && (decisor != null);
		isOK = isOK && (forwardPort != 0);
		isOK = isOK && (backwardPort != 0);
		isOK = isOK && (destination != null);

		return isOK;
	}

	private void visualizeFrame(byte[] rawFrame,int id) {
		visualizer.display(rawFrame, id);
	}
	private void cleanUp(){
		cam.close();
		
		if(LOG_LEVEL >= LOG.Debug)
			Log.i(logWriter,"TxMain", "exiting");
	}

	public boolean isRunning(){
		return RUNNING;
	}

	public void stopRunning(){
		RUNNING = false;
	}
	public void setLogWriter(Writer logWriter) {
		this.logWriter = logWriter;
	}

}
