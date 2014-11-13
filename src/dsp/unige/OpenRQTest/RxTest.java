package dsp.unige.OpenRQTest;

import javax.swing.JLabel;

import dsp.unige.ALC.utils.ActualVisualizer;
import dsp.unige.ALC.utils.Constants;
import dsp.unige.alc.rx.RxMain;

public class RxTest {

	public void go(JLabel l){
		RxMain rxMain = new RxMain();
		rxMain.init();
		
		ActualVisualizer visualizer = new ActualVisualizer(l);
//		visualizer.init("RECEIVER");
		
		rxMain.setForwardPort(Constants.FORWARD_PORT);
		rxMain.setBackwardPort(Constants.BACKWARD_PORT);
		rxMain.setVisualizer(visualizer);
		
		rxMain.go();
	}
}
