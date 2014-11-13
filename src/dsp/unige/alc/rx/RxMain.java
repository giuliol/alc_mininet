package dsp.unige.alc.rx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.fec.openrq.parameters.FECParameters;
import dsp.unige.ALC.utils.Constants;
import dsp.unige.ALC.utils.Log;
import dsp.unige.ALC.utils.Packet;
import dsp.unige.ALC.utils.PacketBuffer;
import dsp.unige.ALC.utils.RQDecoder;
import dsp.unige.ALC.utils.Visualizer;

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

	public void go(){

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
					if(img.id==24){
						try {
							File file = new File("outputs/24RX.jpg");
							FileOutputStream fos = null;
							try {
								fos = new FileOutputStream(file);
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							}
							fos.write(img.bytes);
							fos.flush();
							fos.close();
							System.out.println("RxMain.go() SCRITTO 24");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				try {
					Thread.sleep(1000/Constants.FPS);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.i("RxMain","error sleeping");
				}
			}
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
