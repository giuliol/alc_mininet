package dsp.unige.OpenRQTest;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class MainClass {
	static JFrame frame;
	static JPanel panel;
	static JLabel lTx;
	static JLabel lRx;
	public static void main(String[] args) {
//		RQTest t1 = new RQTest();
		
//		double errc=0;
//		int ITERATIONS = 10;
//		t1.go(20,0.3);
//		System.out.println("RQ codec ok");
//		for(int k=34;k>0;k--){
//			errc=0;
//			for(int i=0;i<ITERATIONS;i++)
//				errc+=t1.go(k,0.3);
//			errc=errc/ITERATIONS;
//			System.out.println("Raptor codec ok:"+String.format("%6.2f",((double)k/35))+" errc: "+errc);
//		}
//		
//		JPEGTest t2 = new JPEGTest();
//			t2.go();
//		System.out.println("JPEG codec ok");

		

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
}
