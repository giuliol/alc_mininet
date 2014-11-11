package dsp.unige.ALC.TX;

import java.net.Inet4Address;

import dsp.unige.ALC.utils.Camera;
import dsp.unige.ALC.utils.DummyCamera;
import dsp.unige.ALC.utils.JpegEncoder;
import dsp.unige.ALC.utils.RQEncoder;

public class TxMain {
	
	private static final int CWLEN = 35;
	private static final int PKTSIZE = 1024;
	private static final int CWBSIZE = 15;

	private RQEncoder rqEnc;
	private Camera cam;
	private boolean RUNNING;
	private PacketBuffer pBuffer;
	private CodeWordBuffer cwBuffer;
	private Decisor decisor;
	
	public int Q;
	public int FEC;
	public Object BufferLock;
	private Inet4Address destination;
	private int destinationPort;

	public void setDestination(Inet4Address destination) {
		this.destination = destination;
	}
	public void setDestinationPort(int destinationPort) {
		this.destinationPort = destinationPort;
	}
	
	public void init(String videoFile){

		int bpf = Constants.BPF;
		int fps = Constants.FPS;
		
		cam = new DummyCamera();
		((DummyCamera)cam).init(videoFile,bpf,fps);
		Q = 0;
		BufferLock = new Object();
		pBuffer = new PacketBuffer();
		pBuffer.init(CWLEN * PKTSIZE);
		cwBuffer = new CodeWordBuffer();
		cwBuffer.init(CWBSIZE);
		RUNNING = true;
	}

	public void go(){
	
		cam.open();
		
		byte [] rawFrame;
		byte [] compressedFrame;
		byte [][] packetsBytes;
		CodeWord word;
		int contentId = 0;
		int codeWordNumber = 0;
		
		StreamerThread st = new StreamerThread(cwBuffer);
		st.setDestination(destination, destinationPort);
		st.start();
		
		ListenerThread lt = new ListenerThread(cwBuffer);
		lt.setDecisor(decisor);
		lt.start();
		
		while( cam.hasFrame() && isRunning() ){
			rawFrame = cam.getFrame();
			compressedFrame = JpegEncoder.Compress(rawFrame, Q);
			if(pBuffer.has(compressedFrame.length))		
			// keep filling the packet buffer
			{ 
				pBuffer.put(Packet.fromByteArray(compressedFrame, contentId++ ));
			}
			else		
			// buffer full, encode and handle to codeword buffer
			{
				packetsBytes = rqEnc.encode(pBuffer.getData(), FEC);
				word = CodeWord.fromPacketArray(Packet.from2DByteArray(packetsBytes), FEC, codeWordNumber++);
				cwBuffer.put(word);
			}
		}
		
		cleanUp();
	}
	
	private void cleanUp(){
		cam.close();
	}

	public boolean isRunning(){
		return RUNNING;
	}
	
	
}
