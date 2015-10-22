package dsp.unige.alc.rx;

import java.io.Writer;

import net.fec.openrq.parameters.FECParameters;
import dsp.unige.alc.common.Constants;
import dsp.unige.alc.common.Log;
import dsp.unige.alc.common.Packet;
import dsp.unige.alc.common.RQDecoder;
import dsp.unige.alc.common.Visualizer;

public class RxMain {

	private static final int CWLEN = Constants.CWLEN;
	private static final int PKTSIZE = Packet.PKTSIZE;

	private int backwardPort;
	private boolean RUNNING = true;
	private int forwardPort;
	private RQDecoder dec;
	private FECParameters fp;
	private ImageBuffer imageBuffer;
	private Visualizer visualizer;
	private Writer logWriter;
	private String info;
	
	public void setVisualizer(Visualizer visualizer) {
		this.visualizer = visualizer;
	}

	public void setBackwardPort(int destinationPort) {
		this.backwardPort = destinationPort;
	}

	public void setForwardPort(int localPort) {
		this.forwardPort = localPort;
	}

	public void stopRunning(){
		RUNNING = false;
	}

	public void init(){

		//		visualizer = new DummyVisualizer();
		//		((DummyVisualizer)visualizer).init("outputs/received.jps");
		imageBuffer = new ImageBuffer();
		imageBuffer.init(Constants.IMAGE_BUFFER_SIZE);

	}

	public void go() throws InterruptedException{

		fp = FECParameters.newParameters(CWLEN * PKTSIZE, PKTSIZE, 1);
		dec = new RQDecoder();
		dec.init(fp);


		if(checkAll()){

			ReceiverThread rt = new ReceiverThread(imageBuffer, dec);
			rt.setLogWriter(logWriter);
			rt.setForwardPort(forwardPort);
			rt.setBackwardPort(Constants.FEEDBACK_PORT);
			rt.start();
			

			TerminatorThread tt = new TerminatorThread();
			tt.setCallerHandle(this);
			tt.start();
			
			TaggedImage img;
			while(RUNNING || imageBuffer.hasToVisualize()){
				if(imageBuffer.hasToVisualize()){
					img = imageBuffer.get();
//					info = " R:"+
					visualizer.display(img.bytes, img.id, rt.getInfo());
				}
				try {
					Thread.sleep(Math.round(1000d/(Constants.FPS)));
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.i(logWriter,"RxMain","error sleeping");
				}
			}
			rt.stopRunning();
			rt.join();
			tt.join();
			Log.i(logWriter,"RxMain","Test terminated correctly, exiting");
		}
		else{
			Log.i(logWriter,"RxMain","checkAll failed");
		}
		cleanUp();

	}

	private boolean checkAll() // checks if everything is set
	{
		boolean isOK = true;

		isOK = isOK && (visualizer != null);
		isOK = isOK && (forwardPort != 0);
		isOK = isOK && (backwardPort != 0);
		isOK = isOK && (logWriter != null);

		return isOK;
	}
	private void cleanUp(){

	}

	public void setWriter(Writer logWriter) {
		this.logWriter = logWriter;
	}
}
