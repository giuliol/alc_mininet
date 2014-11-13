package dsp.unige.alc.tx;

import dsp.unige.ALC.utils.Constants;

public class Decisor {

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
		//TODO
		return 1;
	}
	
	public int decideQ(double rc){
		int perFrame = (int) Math.round(estimatedRWin.avg() / 8 / Constants.FPS * rc);
		System.out.println("Decisor.decideQ() per frame "+perFrame);
		int dec = (int) Math.round(100d*(1-Math.exp((100d-perFrame)/3000d)));
		return Math.max(Math.min(dec, 99),0);
//		return 20;
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
}
