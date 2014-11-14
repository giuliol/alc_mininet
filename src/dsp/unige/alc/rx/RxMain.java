package dsp.unige.alc.rx;

import net.fec.openrq.parameters.FECParameters;
import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Log;
import dsp.unige.alc.utils.Packet;
import dsp.unige.alc.utils.PacketBuffer;
import dsp.unige.alc.utils.RQDecoder;
import dsp.unige.alc.utils.Visualizer;

public class RxMain {

	private static final int CWLEN = Constants.CWLEN;
	private static final int PKTSIZE = Packet.PKTSIZE;

	private int backwardPort;
	private boolean RUNNING = true;
	private PacketBuffer pBuffer;
	private int forwardPort;
	private RQDecoder dec;
	private FECParameters fp;
	private ImageBuffer imageBuffer;
	private Visualizer visualizer;


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

		pBuffer = new PacketBuffer();
		pBuffer.init(CWLEN);
		//		visualizer = new DummyVisualizer();
		//		((DummyVisualizer)visualizer).init("outputs/received.jps");
		imageBuffer = new ImageBuffer();
		imageBuffer.init(3 * Constants.FPS);

	}

	public void go() throws InterruptedException{

		fp = FECParameters.newParameters(CWLEN * PKTSIZE, PKTSIZE, 1);
		dec = new RQDecoder();
		dec.init(fp);


		if(checkAll()){

			ReceiverThread rt = new ReceiverThread(imageBuffer, dec);
			rt.setForwardPort(forwardPort);
			rt.setBackwardPort(Constants.BACKWARD_PORT);
			rt.start();

			TaggedImage img;
			while(RUNNING){
				if(imageBuffer.hasToVisualize()){
					img = imageBuffer.get();
					visualizer.display(img.bytes, img.id);
				}
				try {
					Thread.sleep(Math.round(1000d/(Constants.FPS*1.05d)));
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.i("RxMain","error sleeping");
				}
			}
			rt.stopRunning();
			rt.join();
		}
		else{
			Log.i("RxMain","checkAll failed");
		}
		cleanUp();

	}

	private boolean checkAll() // checks if everything is set
	{
		boolean isOK = true;

		isOK = isOK && (visualizer != null);
		isOK = isOK && (forwardPort != 0);
		isOK = isOK && (backwardPort != 0);

		return isOK;
	}
	private void cleanUp(){

	}
}
