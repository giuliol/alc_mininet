package dsp.unige.ALC.TX;

import dsp.unige.ALC.utils.RQEncoder;

public class EncodingThread extends Thread{

	// handles
	TxMain mainClassHandle;
	RQEncoder rqEnc;
	
	public EncodingThread(TxMain h){
		mainClassHandle = h;
		rqEnc = new RQEncoder();

	}
	
	@Override
	public void run() {
		super.run();
		
		byte[] buffer;
		byte[][] packets;
		while( mainClassHandle.isRunning() ){
			buffer = mainClassHandle.getBuffer();
			packets = rqEnc.encode(buffer, mainClassHandle.FEC);
		}
		
	}
}
