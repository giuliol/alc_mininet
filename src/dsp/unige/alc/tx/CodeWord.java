package dsp.unige.alc.tx;

import dsp.unige.ALC.utils.Constants;
import net.fec.openrq.parameters.FECParameters;

public class CodeWord {
	
	public static final int CWLEN = Constants.CWLEN;
	Packet[] pkts;
	int codeWordNumber;
	FECParameters fp;
	boolean available;
	
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
		return out;
		
	}
}