package dsp.unige.alc.tx;

public class SessionParameters {
	
	private int Q;
	private int FEC;
	
	public SessionParameters(){
		Q=0;
		FEC=0;
	}

	public void setQ(int q) {
		Q = q;
	}
	
	public void setFEC(int fEC) {
		FEC = fEC;
	}
	
	public int getFEC() {
		return FEC;
	}
	
	public int getQ() {
		return Q;
	}

}
