package dsp.unige.artifacts;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dsp.unige.OpenRQTest.RxTest;
import dsp.unige.OpenRQTest.TxTest;
import dsp.unige.alc.utils.Constants;

public class Test {
	
	static JFrame frame;
	static JPanel panel;
	static JLabel lTx;
	static JLabel lRx;
	public static void main(String[] args) {
		frame = new JFrame("");
		panel = new JPanel(new BorderLayout());
		lTx = new JLabel();
		lRx = new JLabel();
		panel.add(lTx, BorderLayout.WEST);
		panel.add(lRx, BorderLayout.EAST);
		panel.setVisible(true);
		frame.add(panel);
		frame.setVisible(true);
		frame.setSize((int) (Constants.WIDTH*2.5)  , (int) (Constants.HEIGHT*1.5));

		System.out.println("MainClass.main() RX start");
		(new Thread(new Runnable() {

			@Override
			public void run() {
				RxTest rtest = new RxTest();
				rtest.setPath(0);
				rtest.goWithDummyScreen();				
			}
		})).start();


		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("MainClass.main() TX start");
		TxTest ttest = new TxTest();
		ttest.setPath(0);
		ttest.goWithDummyScreen("127.0.0.1");
	}
}
