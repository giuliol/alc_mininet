package dsp.unige.ALC.TX;

import net.fec.openrq.parameters.FECParameters;

public class Packet {

	public static final int PKTSIZE = 1024;
	public static final int HEADERSIZE = 6 * Integer.SIZE/8;
	
	byte[] data;
	int sequenceNumber;
	int codeWordNumber;
	int FEC;
	int contentId;
	int contentOffset;
	int contentSize;
	
	public Packet(){
		data = new byte[PKTSIZE];
		sequenceNumber = -1;
		codeWordNumber = -1;
		FEC = -1;
		contentId = -1;
		contentOffset = -1;
		contentSize = -1;
	}
	
	public static Packet[] from2DByteArray(byte [][] array){
		return null;
	}
	
	public boolean isValid(){
		return (
				FEC!=-1 &&
				sequenceNumber != -1 &&
				codeWordNumber != -1 &&
				contentId != -1 &&
				contentOffset != -1 &&
				contentSize != -1
				);
	}
	
	public static Packet[] fromByteArray(byte[] img, int contentId){
		int required = (int) Math.ceil(img.length / PKTSIZE) ;
		Packet [] out = new Packet[required];
		int bytesLeft;
		for(int i=0;i<required;i++){
			out[i] = new Packet();
			bytesLeft = img.length - PKTSIZE*i;
			System.arraycopy(img, i*PKTSIZE, out[i].data, 0, Math.min(PKTSIZE,bytesLeft));
			out[i].contentId = contentId;
			out[i].contentSize = img.length;
			out[i].contentOffset = i*PKTSIZE;
		}
		return out;
	}
}
