package dsp.unige.OpenRQTest;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JLabel;

import dsp.unige.alc.tx.TxMain;
import dsp.unige.alc.utils.ActualVisualizer;
import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Constants.LOG;

public class TxTest {

	public void go(JLabel l){
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
}
