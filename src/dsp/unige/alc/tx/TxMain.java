package dsp.unige.alc.tx;

import java.awt.HeadlessException;
import java.net.InetAddress;

import dsp.unige.ALC.utils.Camera;
import dsp.unige.ALC.utils.CodeWord;
import dsp.unige.ALC.utils.Constants;
import dsp.unige.ALC.utils.Constants.LOG;
import dsp.unige.ALC.utils.DummyCamera;
import dsp.unige.ALC.utils.JpegEncoder;
import dsp.unige.ALC.utils.Log;
import dsp.unige.ALC.utils.Packet;
import dsp.unige.ALC.utils.PacketBuffer;
import dsp.unige.ALC.utils.RQDecoder;
import dsp.unige.ALC.utils.RQEncoder;
import dsp.unige.ALC.utils.Visualizer;

public class TxMain {

	private static final int CWLEN = Constants.CWLEN;
	private static final int PKTSIZE = Packet.PKTSIZE;
	private static final int CWBSIZE = 15;

	private RQEncoder rqEnc;
	private Camera cam;
	private boolean RUNNING;
	private PacketBuffer pBuffer;
	private CodeWordBuffer cwBuffer;
	private Decisor decisor;

	private InetAddress destination;
	private int forwardPort;
	private int backwardPort;
	private int LOG_LEVEL = 0;
	private SessionParameters sessionParameters;
	private Visualizer visualizer;

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

		pBuffer = new PacketBuffer();
		pBuffer.init(CWLEN);
		cwBuffer = new CodeWordBuffer();
		cwBuffer.init(CWBSIZE);
		RUNNING = true;

		rqEnc = new RQEncoder();
		rqEnc.init(CWLEN * PKTSIZE, PKTSIZE);

		sessionParameters = new SessionParameters();
		sessionParameters.setQ(20);
		sessionParameters.setFEC(1);

		decisor = new Decisor();

	}

	public void setVisualizer(Visualizer visualizer) {
		this.visualizer = visualizer;
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
		st.setDestination(destination, forwardPort);
		st.start();

		ListenerThread lt = new ListenerThread(cwBuffer,sessionParameters);
		lt.setDecisor(decisor);
		lt.setBackwardPort(backwardPort);
		lt.start();
		sessionParameters.setFEC(1);

		int framesInCodeword =0 ;
		if(checkAll())
			while( cam.hasFrame() && isRunning() ){
				rawFrame = cam.getFrame();
				contentId++;
//				System.out.println("TxMain.go() using Q ="+sessionParameters.getQ());
				compressedFrame = JpegEncoder.Compress(rawFrame, sessionParameters.getQ(),Constants.WIDTH, Constants.HEIGHT);
				visualizeFrame(JpegEncoder.Compress(rawFrame, 100 ,Constants.WIDTH, Constants.HEIGHT),contentId);
				if(pBuffer.hasBytesAvailable(compressedFrame.length))		
					// keep filling the packet buffer
				{ 
					pBuffer.put(Packet.fromByteArray(compressedFrame, contentId ));
					framesInCodeword++;
				}
				else		
					// buffer full, encode and handle to codeword buffer
				{
					System.out.println("TxMain.go() got "+framesInCodeword+ " frames in last cw. Estimated "+(framesInCodeword* decisor.getRate() / (CWLEN *  (PKTSIZE+24+RQDecoder.HEADERSIZE) *8)));
					packetsBytes = rqEnc.encode(pBuffer.getData(), sessionParameters.getFEC());
					pBuffer.fillWithEncoded(packetsBytes);
					word = CodeWord.fromPacketArray(pBuffer.getPackets(), sessionParameters.getFEC(), codeWordNumber++);
					cwBuffer.put(word);
					pBuffer.reset();
					pBuffer.put(Packet.fromByteArray(compressedFrame, contentId ));
					framesInCodeword = 1;
				}
			}

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
			Log.i("TxMain", "exiting");
	}

	public boolean isRunning(){
		return RUNNING;
	}

	public void stopRunning(){
		RUNNING = false;
	}

}
