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

		int Q;
		HashMap<String, Double>  results ;
		
		File csv =  new File("benchmarks/q_size_ssim.csv");
		OutputStreamWriter osw = null;
		
		try {
			 osw =  new OutputStreamWriter(new FileOutputStream(csv),Charset.forName("UTF-8"));
			 osw.write("Q ; Average_Size ; Average_SSIM\n");
			 for(Q=0;Q<=100;Q++){
				 results = benchmarkJpeg(Q);
				 osw.write(String.format("%d ; %6.4f ; %6.4f\n",(int)results.get("Q").doubleValue(),results.get("AvgSize").doubleValue(),results.get("AvgSSIM").doubleValue()));
				 osw.flush();
			 }
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static HashMap<String, Double> benchmarkJpeg(int Q){
		JpegCoderBenchmark jcb = new JpegCoderBenchmark();
		long tic = System.currentTimeMillis();
		HashMap<String, Double> result = jcb.testAll(Q);
		System.out.println("benchmark for Q="+Q+", "+(System.currentTimeMillis() - tic)/1000d+" seconds");
		System.out.println(result);
		return result;
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
