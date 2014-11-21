package dsp.unige.alc.utils;

import net.fec.openrq.ArrayDataEncoder;
import net.fec.openrq.parameters.FECParameters;

public class RQEncoder {

	int SIZE,PKTSIZE;
	FECParameters fp;
	
	public RQEncoder(){
		
	}
	
	public void init(int mSize, int mPktSize){
		SIZE = mSize;
		PKTSIZE = mPktSize;
		fp = FECParameters.newParameters(SIZE, PKTSIZE, 1);
	}
	
	public FECParameters getFecParameters() {
		return fp;
	}
	
	public byte [][]encode(byte[] data, int FEC){
		ArrayDataEncoder ade = ORQcore.getEncoder(data,fp);
		return ORQcore.encodeBlock(ade, 0,FEC);
		
	}
}
