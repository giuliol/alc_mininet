package dsp.unige.OpenRQTest;

import java.util.Date;

import javax.swing.JLabel;

import dsp.unige.alc.rx.RxMain;
import dsp.unige.alc.utils.ActualVisualizer;
import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.DoubleVisualizer;

public class RxTest {
	
	int path;

	public void setPath(int path) {
		this.path = path;
	}

	public void goWithScreen(JLabel l){
		RxMain rxMain = new RxMain();
		rxMain.init();
		
		ActualVisualizer visualizer = new ActualVisualizer(l);
//		visualizer.init("RECEIVER");
		
		rxMain.setForwardPort(Constants.FORWARD_PORT);
		rxMain.setBackwardPort(Constants.BACKWARD_PORT);
		rxMain.setVisualizer(visualizer);
		
		try {
			rxMain.go();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	public void goWithDummyScreen(){
		RxMain rxMain = new RxMain();
		rxMain.init();
		
		DoubleVisualizer visualizer = new DoubleVisualizer();
		visualizer.init(Constants.RECEIVED_JPS_FILENAME , "RECEIVER");
		
		rxMain.setForwardPort(Constants.FORWARD_PORT);
		rxMain.setBackwardPort(Constants.BACKWARD_PORT);
		rxMain.setVisualizer(visualizer);
		
		Date d =  new Date(System.currentTimeMillis());
		System.out.println("### "+d.toString());
		System.out.println("Receiver for path "+path);
		System.out.println("Starting receiver on port "+Constants.FORWARD_PORT);
		System.out.println("Will feedback at transmitter on port "+Constants.BACKWARD_PORT);
		System.out.println("Writing video frames in "+Constants.RECEIVED_JPS_FILENAME);
		System.out.println("### ");


		try {
			rxMain.go();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		visualizer.close();
	}
}
