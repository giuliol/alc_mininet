package dsp.unige.ALC.TX;

import dsp.unige.ALC.utils.JpegEncoder;

public class AcquisitionThread extends Thread{

	// handles
	TxMain mainClassHandle;
	
	
	public AcquisitionThread(TxMain h) {
		mainClassHandle = h;
	}
	
	@Override
	public void run() {
		super.run();
		
		byte[] rawFrame;
		byte[] compressedFrame;
		
		

	}
	
}
