package dsp.unige.OpenRQTest;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dsp.unige.ALC.utils.Constants;
import dsp.unige.alc.benchmark.JpegCoderBenchmark;


public class MainClass {
	static JFrame frame;
	static JPanel panel;
	static JLabel lTx;
	static JLabel lRx;
	public static void main(String[] args) {

		benchmarkJpeg();
	}
	
	public static void benchmarkJpeg() {
		JpegCoderBenchmark jcb = new JpegCoderBenchmark();
		jcb.go();
	}


	
	public static void benchmarkRQ(){
		
	}
	
	public static void testRX_and_TX(){
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
				rtest.go(lRx);				
			}
		})).start();
	
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("MainClass.main() TX start");
		TxTest ttest = new TxTest();
		ttest.go(lTx);
	}
	
	public static void testJpeg(){
		
		JPEGTest t2 = new JPEGTest();
			t2.go();
		System.out.println("JPEG codec ok");
	}
	
	public static void testRQeg(){
		RQTest t1 = new RQTest();
		
//		double errc=0;
//		int ITERATIONS = 10;
		t1.go(20,0.3);
		System.out.println("RQ codec ok");
//		for(int k=34;k>0;k--){
//			errc=0;
//			for(int i=0;i<ITERATIONS;i++)
//				errc+=t1.go(k,0.3);
//			errc=errc/ITERATIONS;
//			System.out.println("Raptor codec ok:"+String.format("%6.2f",((double)k/35))+" errc: "+errc);
//		}
	}
}
