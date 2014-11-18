package dsp.unige.OpenRQTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.swing.JLabel;

import dsp.unige.alc.tx.TxMain;
import dsp.unige.alc.utils.ActualVisualizer;
import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Constants.LOG;
import dsp.unige.alc.utils.DoubleVisualizer;
import dsp.unige.alc.utils.Log;

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
		
		Writer logWriter = null;
		try {
			logWriter = new FileWriter(new File(path+"/"+Constants.TRANSMITTER_LOGFILE));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		tx.setForwardPort(Constants.FORWARD_PORT);
		tx.setBackwardPort(Constants.BACKWARD_PORT);
		tx.setLogLevel(LOG.Debug);
		tx.setLogWriter(logWriter);
		tx.init(videoFile);
		tx.setVisualizer(visualizer);
		
		Date d =  new Date(System.currentTimeMillis());
		Log.i(logWriter,"INIT","### "+d.toString());
		Log.i(logWriter,"INIT","Receiver for path "+path);
		Log.i(logWriter,"INIT","Starting transmitter. Sending frames to "+dest +" on port "+Constants.FORWARD_PORT);
		Log.i(logWriter,"INIT","Will listen for feedback on port "+Constants.BACKWARD_PORT);
		Log.i(logWriter,"INIT","Dummy camera opened video file "+videoFile);
		Log.i(logWriter,"INIT","Writing reference video frames on "+Constants.REFERENCE_JPS_FILENAME);
		Log.i(logWriter,"INIT","### ");

		try {
			tx.go();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		visualizer.close();
	}
}
