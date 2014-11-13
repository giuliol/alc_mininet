package dsp.unige.alc.tx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

		if(checkAll())
			while( cam.hasFrame() && isRunning() ){
				rawFrame = cam.getFrame();
				contentId++;
				compressedFrame = JpegEncoder.Compress(rawFrame, sessionParameters.getQ(),Constants.WIDTH, Constants.HEIGHT);
//				System.out.println("TxMain.go() frame "+contentId+", "+compressedFrame[0]+" "+compressedFrame[1]);
//				System.out.println("TxMain.go() frame "+contentId+", size "+compressedFrame.length);
				visualizeFrame(JpegEncoder.Compress(rawFrame, 100 ,Constants.WIDTH, Constants.HEIGHT),contentId);
//				visualizeFrame(compressedFrame, contentId);
				if(contentId == 24){
					try {
						File file = new File("outputs/24TX.jpg");
						FileOutputStream fos = null;
						try {
							fos = new FileOutputStream(file);
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
						fos.write(JpegEncoder.Compress(rawFrame, 20 ,Constants.WIDTH, Constants.HEIGHT));
						fos.flush();
						fos.close();
						System.out.println("TxMain.go() SCRITTO 24");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if(pBuffer.hasBytesAvailable(compressedFrame.length))		
					// keep filling the packet buffer
				{ 
					pBuffer.put(Packet.fromByteArray(compressedFrame, contentId ));
				}
				else		
					// buffer full, encode and handle to codeword buffer
				{
					if(codeWordNumber==0)
						write(pBuffer.getData());
					packetsBytes = rqEnc.encode(pBuffer.getData(), sessionParameters.getFEC());
					pBuffer.fillWithEncoded(packetsBytes);
					word = CodeWord.fromPacketArray(pBuffer.getPackets(), sessionParameters.getFEC(), codeWordNumber++);
					cwBuffer.put(word);
					pBuffer.reset();
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

	private void write(byte[] decodedArray) {
		
		File file = new File ("outputs/cw1_decArrayTX.dat");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			fos.write(decodedArray);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
