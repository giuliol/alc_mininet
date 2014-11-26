package dsp.unige.alc.utils;

import net.fec.openrq.ArrayDataDecoder;
import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.Parsed;
import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.parameters.FECParameters;

public class RQDecoder {
	int SIZE,PKTSIZE;
	FECParameters fp;
	ArrayDataDecoder add;
	public static final int DATA_DECODE_COMPLETE = 0;
	public static final int DATA_NEED_MORE = 1;
	public static final int DATA_DECODE_FAILURE = -1;
	public static final int UNKNOWN_HANDLEPACKET_ERROR = -2;
	public static final int INVALID_PACKET = -3;
	public static final int HEADERSIZE = 8;


	public RQDecoder() {

	}
	
	
	public void init(FECParameters fpp){
		fp = fpp;
		add = OpenRQ.newDecoderWithTwoOverhead(fp);
	}
	
	public FECParameters getFecParameters() {
		return fp;
	}
	
	public int handlePacket(byte [] received){
		
		Parsed<EncodingPacket> parsedPac = add.parsePacket(received, false);
		if(parsedPac.isValid()){
			return handle(parsedPac.value());
		}
		return INVALID_PACKET;
	}

	
	private int handle(EncodingPacket value) {
		int sbn = value.sourceBlockNumber();
		SourceBlockDecoder sbDec =  add.sourceBlock(sbn);
		switch (sbDec.putEncodingPacket(value)) {
		case DECODED:
//			Log.i("RQDecoder.handle()","successfully decoded");
			return DATA_DECODE_COMPLETE;
		case INCOMPLETE:
//			Log.i("Test.handlePacket()","need more encoding packets!");
			return DATA_NEED_MORE;
		case DECODING_FAILURE:
			Log.i("Test.handlePacket()","decode failure");
			return DATA_DECODE_FAILURE;
		default:
			return UNKNOWN_HANDLEPACKET_ERROR;
		}
	}

	public byte[] getDataAsArray(){
		return add.dataArray();
	}

	public boolean isDecoded() {
		return add.isDataDecoded();
	}

}
