package dsp.unige.ALC.TX;

import java.net.Inet4Address;
import java.nio.ByteBuffer;

import dsp.unige.ALC.utils.Camera;
import dsp.unige.ALC.utils.DummyCamera;
import dsp.unige.ALC.utils.JpegEncoder;
import dsp.unige.ALC.utils.RQEncoder;

public class TxMain {
	
	private static final int CWLEN = 35;
	private static final int PKTSIZE = 1024;
	private static final int CWBSIZE = 15;

	private Inet4Address destinationIp;
	private JpegEncoder jpgEnc;
	private RQEncoder rqEnc;
	private Camera cam;
	private boolean RUNNING;
	private PacketBuffer pBuffer;
	private CodeWordBuffer cwBuffer;
	

	public int Q;
	public int FEC;
	public Object BufferLock;

	
	public void init(String videoFile){

		int bpf = 38016;
		int fps = 25;
		
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
		Packet[] packets;
		CodeWord word;
		int contentId = 0;
		
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
				word = CodeWord.fromPacketArray(Packet.from2DByteArray(packetsBytes));
			   //synchronize on cwbuffer!!
			   cwBuffer.put(word):
			}
		}
	}
	
	private void cleanUp(){
		cam.close();
	}


	public boolean isRunning(){
		return RUNNING;
	}
	
	
}
