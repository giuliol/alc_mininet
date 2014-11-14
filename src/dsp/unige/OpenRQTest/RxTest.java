package dsp.unige.OpenRQTest;

import javax.swing.JLabel;

import dsp.unige.alc.rx.RxMain;
import dsp.unige.alc.utils.ActualVisualizer;
import dsp.unige.alc.utils.Constants;

public class RxTest {

	public void go(JLabel l){
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
}
