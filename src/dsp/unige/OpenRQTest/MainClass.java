package dsp.unige.OpenRQTest;

import java.net.InetAddress;
import java.net.UnknownHostException;

import dsp.unige.ALC.utils.Constants.LOG;
import dsp.unige.alc.tx.TxMain;





public class MainClass {

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

		TxMain tx =  new TxMain();
		try {
			tx.setDestination(InetAddress.getByName("130.251.18.52"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("MainClass.main() error setting address");
		}
		tx.setDestinationPort(5558);
		tx.setLogLevel(LOG.Debug);
		tx.init("video/highway_qcif.yuv");
		tx.go();
		
		
	}
}
