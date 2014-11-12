package dsp.unige.OpenRQTest;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JLabel;

import dsp.unige.ALC.utils.ActualVisualizer;
import dsp.unige.ALC.utils.Constants;
import dsp.unige.ALC.utils.Constants.LOG;
import dsp.unige.alc.tx.TxMain;

public class TxTest {

	public void go(JLabel l){
		TxMain tx =  new TxMain();
		try {
			tx.setDestination(InetAddress.getByName("127.0.0.1"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("MainClass.main() error setting address");
		}
		
		ActualVisualizer visualizer = new ActualVisualizer();
		visualizer.init("TRANSMITTER");
		
		tx.setForwardPort(Constants.FORWARD_PORT);
		tx.setBackwardPort(Constants.BACKWARD_PORT);
		tx.setLogLevel(LOG.Debug);
		tx.init("video/highway_qcif.yuv");
		tx.setVisualizer(visualizer);
		tx.go();
	}
}
