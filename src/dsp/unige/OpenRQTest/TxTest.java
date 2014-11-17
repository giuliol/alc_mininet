package dsp.unige.OpenRQTest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.swing.JLabel;

import dsp.unige.alc.tx.TxMain;
import dsp.unige.alc.utils.ActualVisualizer;
import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Constants.LOG;
import dsp.unige.alc.utils.DoubleVisualizer;

public class TxTest {

	int path;
	
	public void setPath(int path) {
		this.path = path;
	}
	
	public void goWithScreen(JLabel l){
		TxMain tx =  new TxMain();
		try {
			tx.setDestination(InetAddress.getByName("127.0.0.1"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("MainClass.main() error setting address");
		}
		
		ActualVisualizer visualizer = new ActualVisualizer(l);
//		visualizer.init("TRANSMITTER");

		
		tx.setForwardPort(Constants.FORWARD_PORT);
		tx.setBackwardPort(Constants.BACKWARD_PORT);
		tx.setLogLevel(LOG.Debug);
		tx.init("video/highway_qcif.yuv");
		tx.setVisualizer(visualizer);
		try {
			tx.go();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void goWithDummyScreen(String dest){
		TxMain tx =  new TxMain();
		try {
			tx.setDestination(InetAddress.getByName(dest));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("TxTest.goWithDummyScreen() not an IP address");
			return;
		}
		
		String videoFile = "video/highway_qcif.yuv";
		
		DoubleVisualizer visualizer = new DoubleVisualizer();
		visualizer.init(path+"/"+Constants.REFERENCE_JPS_FILENAME , "TRANSMITTER, path "+path);
		
		tx.setForwardPort(Constants.FORWARD_PORT);
		tx.setBackwardPort(Constants.BACKWARD_PORT);
		tx.setLogLevel(LOG.Debug);
		tx.init(videoFile);
		tx.setVisualizer(visualizer);
		
		Date d =  new Date(System.currentTimeMillis());
		System.out.println("### "+d.toString());
		System.out.println("Receiver for path "+path);
		System.out.println("Starting transmitter. Sending frames to "+dest +" on port "+Constants.FORWARD_PORT);
		System.out.println("Will listen for feedback on port "+Constants.BACKWARD_PORT);
		System.out.println("Dummy camera opened video file "+videoFile);
		System.out.println("Writing reference video frames on "+Constants.REFERENCE_JPS_FILENAME);
		System.out.println("### ");

		try {
			tx.go();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		visualizer.close();
	}
}
