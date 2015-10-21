package dsp.unige.OpenRQTest;

import java.util.ArrayList;
import java.util.Random;

import dsp.unige.alc.common.RQDecoder;
import dsp.unige.alc.common.RQEncoder;

public class RQTest {
	
	public double go4(int DATA, double PL){

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
		ArrayList<byte[]> list = loseReturnsList(pkts,PL,CWLEN);

		
		
		
//		DECODING
//		
		RQDecoder dec = new RQDecoder();
		dec.init(enc.getFecParameters());
		
		int i;
		System.out.println("RQTest.go4() begin");
		for(i=0;i<list.size() && dec.handlePacket(list.get(i)) != RQDecoder.DATA_DECODE_COMPLETE ;i++)
			;
//			System.out.println("RQTest.go4() "+i);
		if(!dec.isDecoded())
			System.out.println("RQTest.go4() failure");
		else
			System.out.println("RQTest.go4() success");
		double residualError = compareBytes(data,dec.getDataAsArray());
//		System.out.println("RQTest.go() DATA:"+(DATA)+" richiesti "+(i+1)+" iterazioni, byte errati " + residualError);

		return residualError;

	}

	
	private ArrayList<byte[]> loseReturnsList(byte[][] pkts, double PL,int MAX) {
		
		ArrayList<byte[]> received = new ArrayList<byte[]>();
		
		Random r  = new Random(System.currentTimeMillis());
		int lo = 0;
		for (int i = 0; i < MAX; i++) {
			if(pkts[i]!=null)
				if(r.nextDouble()<PL){
					lo++;
					int l = pkts[i].length;
					pkts[i] = new byte[l];
				}
				else
					received.add(pkts[i]);
		}
		return received;
		}

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
		
//		System.out.println("RQTest.go() DATA:"+(DATA)+" richiesti "+(i+1)+" iterazioni");
		return compare(data,dec.getDataAsArray());

	}
	
	//ritorna quanti byte sbagliati....
	private int compareBytes(byte[] data, byte[] dataArray) {

		if(data.length!=dataArray.length)
			System.out.println("Test.compare() lunghezze diverse -cominciamo bene");
		
		int errcount=0;
		for (int i = 0; i < data.length; i++) {
			if(data[i]!=dataArray[i])
				errcount++;
		}
		return errcount;
	}

	//ritorna quanti pacchetti sbagliati....
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
		int lo = 0;
		for (int i = 0; i < MAX; i++) {
			if(pkts[i]!=null)
				if(r.nextDouble()<PL){
					lo++;
					int l = pkts[i].length;
					pkts[i] = new byte[l];
				}
		}
		System.out.println("RQTest.lose() lost "+lo);
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
		System.out.println("RQTest.go2() compiute "+i+" iterazioni");
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
	
	double  go3(int DATA, double PL){

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
		ArrayList<Integer> lost = lose2(pkts,PL,CWLEN);
		
		
		
//		DECODING
//		
		RQDecoder dec = new RQDecoder();
		dec.init(enc.getFecParameters());
		
		int i,j;
		j = 0;
		int res = -1;
		System.out.println("RQTest.go3() passando solo i pacchetti validi");
		for(i=0;i<pkts.length && res != RQDecoder.DATA_DECODE_COMPLETE ;i++){
			if(!lost.contains(i)){
				res = dec.handlePacket(pkts[i]);
				j++;
			}
		}
			
		System.out.println("RQTest.go2() compiute "+j+" iterazioni, decodifica "+dec.isDecoded()+", errati "+compare(data,dec.getDataAsArray()));
		
		
		dec = new RQDecoder();
		dec.init(enc.getFecParameters());
		
		j = 0;
		res = -1;
		System.out.println("RQTest.go3() passando tutti i pacchetti (a zero quelli non validi)");
		for(i=0;i<pkts.length && dec.handlePacket(pkts[i]) != RQDecoder.DATA_DECODE_COMPLETE ;i++){
		}
			
		System.out.println("RQTest.go2() compiute "+i+" iterazioni, decodifica "+dec.isDecoded()+", errati "+compare(data,dec.getDataAsArray()));
		
		
		
		
		return compare(data,dec.getDataAsArray());
	}

	private ArrayList<Integer> lose2(byte[][] pkts, double PL, int MAX) {
		Random r  = new Random(System.currentTimeMillis());
		int lo = 0;
		ArrayList<Integer> persi =  new ArrayList<Integer>();
		for (int i = 0; i < MAX; i++) {
			if(pkts[i]!=null)
				if(r.nextDouble()<PL){
					lo++;
					int l = pkts[i].length;
					pkts[i] = new byte[l];
					persi.add(i);
				}
		}
		System.out.println("RQTest.lose() lost "+lo);
		return persi;
	}

}
