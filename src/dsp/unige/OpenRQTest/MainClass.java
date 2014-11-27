package dsp.unige.OpenRQTest;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dsp.unige.alc.benchmark.JpegCoderBenchmark;
import dsp.unige.alc.benchmark.RQCoderBenchmark;
import dsp.unige.alc.utils.Constants;


public class MainClass {
	static JFrame frame;
	static JPanel panel;
	static JLabel lTx;
	static JLabel lRx;
	public static void main(String[] args) {

//		doBenchmarks();
//		benchmarkRQ();
		RQTest rt  = new RQTest();
		double er;
		for(int i=34;i>0;i--){
			System.out.println("\n\nMainClass.main() DATA = "+i+", FEC ="+(35-i));
			er = rt.go3(i, 0.3);
		}
		
//		testRX_and_TX();  // local test
//		if(args.length==0){
//			System.out.println("error: syntax is\n java -jar transmitter <destination_IP>");
//			return;
//		}
//		else
//			startTx(args[0]);
//		startRx();

	}


	private static void startTx(String dest) {
		TxTest ttest = new TxTest();
		ttest.goWithDummyScreen(dest);
		System.out.println("Transmitter done, exiting");
	}

	private static void startRx() {
		RxTest rtest = new RxTest();
		rtest.goWithDummyScreen();	
		System.out.println("Receiver done, exiting");
	}

	private static void doBenchmarks() {
		Thread jT = new Thread(new Runnable() {

			@Override
			public void run() {
				benchmarkJpeg();
			}
		});
		jT.start();

		Thread rqT = new Thread(new Runnable() {

			@Override
			public void run() {
				benchmarkRQ();		
			}
		});
		rqT.start();
		
		try {
			jT.join();
			rqT.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void benchmarkJpeg() {
		JpegCoderBenchmark jcb = new JpegCoderBenchmark();
		jcb.go();
	}

	public static void benchmarkRQ(){
		RQCoderBenchmark rcb = new RQCoderBenchmark();
		rcb.go();
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
