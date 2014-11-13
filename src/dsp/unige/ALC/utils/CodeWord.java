package dsp.unige.ALC.utils;

import net.fec.openrq.parameters.FECParameters;

public class CodeWord {
	
	public static final int CWLEN = Constants.CWLEN;
	public Packet[] pkts;
	public int codeWordNumber;
	FECParameters fp;
	public boolean available;
	
	public CodeWord(){
		
	}
	
	public static CodeWord fromPacketArray(Packet[] pkts, int fEC, int cwNo) {
		CodeWord out =  new CodeWord();
		for(int i=0;i<pkts.length;i++){
			pkts[i].sequenceNumber = i;
			pkts[i].FEC = fEC;
			pkts[i].codeWordNumber = cwNo;
		}
		
		out.codeWordNumber = cwNo;
		out.fp = FECParameters.newParameters(CodeWord.CWLEN * Packet.PKTSIZE, Packet.PKTSIZE, 1);
		out.pkts = pkts;
		out.available = true;
		
//		System.out.println("CodeWord.fromPacketArray() out.pkts[0].data.length="+out.pkts[0].data.length );
//		System.out.println("CodeWord.fromPacketArray() frame " + out.pkts[0].contentId+", "+out.pkts[0].data[0]);

		return out;
		
	}
}