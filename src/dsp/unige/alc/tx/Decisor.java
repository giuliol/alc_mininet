package dsp.unige.alc.tx;

import dsp.unige.alc.utils.CodeWord;
import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Packet;

public class Decisor {
	
	private static final double[] SIZES_LUT = {1597.8,1597.8,1598.4,1642.8,1727.1,1818.6,1908.5,1998.7,2084.1,2170.2,2254.7,2335.6,2412.9,2497.6,2569.3,2643.5,2717.5,2787.1,2859.8,2922.9,2984.9,3055.3,3120.5,3187,3250.8,3307.4,3368.8,3434.1,3497.5,3550.5,3607.7,3660.4,3716.1,3780.6,3823.2,3887.3,3949.3,3989.8,4053.2,4100.1,4129.6,4207.4,4249.2,4290.7,4362.3,4393.4,4462.6,4492.1,4534.2,4613.1,4628.3,4645.4,4731.9,4780.4,4817.2,4873.4,4930,4998.5,5067.1,5108.5,5170.8,5250.7,5301.5,5391.4,5453.2,5532.9,5617.8,5690.8,5793.8,5883.7,5995.9,6110.2,6185.7,6312.9,6483.6,6521.7,6655.2,6854.8,7026.8,7142.5,7341,7555.6,7759.1,8014,8235.5,8513.9,8829.1,9129.1,9582.1,9914.1,10506,11055,11574,12468,13579,14837,16764,18860,21514,26850,31777};
	private static final int[] Q_LUT = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100};
	
	private static final int[] OPTIMAL_FEC_LUT = {0,5,5,7,7,9,8,9,10,11,11,12,12,13,13,13,14,15,15,15,16,16,17,18,18,18,18,19,19,20,20,20,21,21,22,22,22,23,23,23,24,24,24,24,24,25,26,26,26,26,27,26,27,27,28,28,27,29,29,29,29,29,29,30,30,30,30,30,31,31,31,31,31,32,32,32,32,32,32,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33};

	private ValuesWindow estimatedRWin;
	private ValuesWindow estimatedLossWin;
	
	public Decisor(){
		estimatedLossWin = new ValuesWindow(Constants.DECISOR_WINDOWS_SIZE);
		estimatedRWin = new ValuesWindow(Constants.DECISOR_WINDOWS_SIZE);
	}
	
	public synchronized double updateLoss(double loss){
		return estimatedLossWin.put(loss);
	}
	
	public synchronized double updateRate(double rate){
		return estimatedRWin.put(rate);
	}
	
	public int decideFEC(){
		int idx = (int) Math.round(100d*Math.min(getLoss(),Constants.CWLEN-1)/Constants.CWLEN);
		return Math.max(OPTIMAL_FEC_LUT[idx],1);
	}
	
	public int decideQ(int FEC){
		double codeWordsPerSecond = getRate() / CodeWord.CODEWORD_SIZE;
		int targetFrames = (int) Math.ceil(Constants.FPS/ codeWordsPerSecond);
		double effectivePayload = Packet.NET_PAYLOAD * (Constants.CWLEN - FEC); // in BYTES!!
		
		int perFrame = (int) Math.round(effectivePayload / targetFrames);
		
		int dec = q_r(perFrame);
		return Math.max(Math.min(dec, 99),0);
	}
	
	private int q_r(int perFrame) {
		int i;
		for(i=0;i<=SIZES_LUT.length-2 && SIZES_LUT[i]<perFrame;i++)
			;
		
		double deltai = Math.abs(perFrame - SIZES_LUT[i]);
		double deltaii = Math.abs(perFrame - SIZES_LUT[i+1]);
		if(deltai<deltaii)
			return Q_LUT[i];
		else 
			return Q_LUT[i+1];
	}

	class ValuesWindow {
		double [] values;
		int head;
		boolean empty = true;
		double acc;
		
		public ValuesWindow(int size){
			values = new double[size];
			head = 0;
		}
		
		public double put(double in){
			if(empty){
				empty = false;
				for(int i=0;i<values.length;i++)
					values[i] = in;
				head = (head + 1)%values.length;
				return in;
			}
			else{
				values[head] = in;
				head = (head + 1)%values.length;
				return avg();
			}
		}

		public double avg() {
			acc = 0;
			for (double v : values) {
				acc += v;
			}
			
			return acc / values.length;
		}
	}

	public double getRate() {
		return estimatedRWin.avg();
	}
	
	public double getLoss() {
		return estimatedLossWin.avg();
	}
}
