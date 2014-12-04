package dsp.unige.OpenRQTest;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import javax.swing.JLabel;

import dsp.unige.alc.rx.RxMain;
import dsp.unige.alc.utils.ActualVisualizer;
import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.DoubleVisualizer;
import dsp.unige.alc.utils.Log;

public class RxTest {
	
	int path;

	public void setPath(int path) {
		this.path = path;
	}

	
	
	public void start(boolean HEADLESS){
		RxMain rxMain = new RxMain();
		rxMain.init();
		
		DoubleVisualizer visualizer = new DoubleVisualizer();
		visualizer.init(path+"/"+Constants.RECEIVED_JPS_FILENAME, "RECEIVER, path "+path, HEADLESS);
		
		Writer logWriter = null;
		try {
			logWriter = new FastFileWriter(new File(path+"/"+Constants.RECEIVER_LOGFILE));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		rxMain.setForwardPort(Constants.FORWARD_PORT);
		rxMain.setBackwardPort(Constants.FEEDBACK_PORT);
		rxMain.setVisualizer(visualizer);
		rxMain.setWriter(logWriter);
		
		Date d =  new Date(System.currentTimeMillis());
		Log.i(logWriter,"INIT","### "+d.toString());
		Log.i(logWriter,"INIT","Receiver for path "+path);
		Log.i(logWriter,"INIT","Starting receiver on port "+Constants.FORWARD_PORT);
		Log.i(logWriter,"INIT","Will feedback at transmitter on port "+Constants.FEEDBACK_PORT);
		Log.i(logWriter,"INIT","Writing video frames in "+Constants.RECEIVED_JPS_FILENAME);
		Log.i(logWriter,"INIT","### ");


		try {
			rxMain.go();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			logWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		visualizer.close();
	}
}
