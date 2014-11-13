package dsp.unige.OpenRQTest;

import java.util.Random;

import dsp.unige.ALC.utils.RQDecoder;
import dsp.unige.ALC.utils.RQEncoder;

public class RQTest {

	public double go(int DATA, double PL){

		int PKTSIZE = 1024;
		int PKTS = DATA;
		int SIZE = PKTSIZE*PKTS;
		int CWLEN = 35;
		byte[] data = new byte [SIZE];
		
	
//		CREATE RANDOM DATA
//		
		Random R =  new Random(System.currentTimeMillis());
		R.nextBytes(data);

		
		
		
//		ENCODING 
//		
		RQEncoder enc = new RQEncoder();
		enc.init(SIZE, PKTSIZE);
		byte[][] pkts=enc.encode(data, CWLEN-DATA);

		
		
		
//		RANDOM PACKET LOSS
//		
		lose(pkts,PL,CWLEN);

		
		
		
//		DECODING
//		
		RQDecoder dec = new RQDecoder();
		dec.init(enc.getFecParameters());
		
		int i;
		for(i=0;i<pkts.length && dec.handlePacket(pkts[i]) != RQDecoder.DATA_DECODE_COMPLETE ;i++)
			;
		
		return compare(data,dec.getDataAsArray());

	}

	private double compare(byte[] data, byte[] dataArray) {

		if(data.length!=dataArray.length)
			System.out.println("Test.compare() lunghezze diverse -cominciamo bene");
		
		int errcount=0;
		for (int i = 0; i < data.length; i++) {
			if(data[i]!=dataArray[i])
				errcount++;
		}
		return ((double)errcount / 1024.0);
	}
	private void lose(byte[][] pkts, double PL, int MAX) {
		Random r  = new Random(System.currentTimeMillis());
		for (int i = 0; i < MAX; i++) {
			if(pkts[i]!=null)
				if(r.nextDouble()<PL){
					int l = pkts[i].length;
					pkts[i] = new byte[l];
				}
		}
		
	}

	public double go2(int DATA, int PL) {

		int PKTSIZE = 1024;
		int PKTS = DATA;
		int SIZE = PKTSIZE*PKTS;
		int CWLEN = 35;
		byte[] data = new byte [SIZE];
		

		
		
//		CREATE RANDOM DATA
//		
		Random R =  new Random(System.currentTimeMillis());
		R.nextBytes(data);

		
		
		
//		ENCODING 
//		
		RQEncoder enc = new RQEncoder();
		enc.init(SIZE, PKTSIZE);
		byte[][] pkts=enc.encode(data, CWLEN-DATA);
		
		
		
		
//		RANDOM PACKET LOSS
//		
		loseSome(pkts, PL);
		
		
		
//		DECODING
//		
		RQDecoder dec = new RQDecoder();
		dec.init(enc.getFecParameters());
		
		int i;
		for(i=0;i<pkts.length && dec.handlePacket(pkts[i]) != RQDecoder.DATA_DECODE_COMPLETE ;i++)
			;

		return compare(data,dec.getDataAsArray());
	}

	void loseSome(byte[][] pkts, int losses){
		Random r=new Random(System.currentTimeMillis());
		int index=0;
		int[] lost=new int[losses];
		int ALPKTSIZE = pkts[0].length;
		boolean redo;

		for(int i=0;i<losses;i++){
			redo=true;
			while(redo){
				index=r.nextInt(pkts.length);
				redo=false;
				for(int k=0;k<i;k++)
					if(index==lost[k])
						redo=true;
			}
			lost[i]=index;
			pkts[index]=new byte[ALPKTSIZE];
		}
	}
}
