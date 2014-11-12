package dsp.unige.ALC.utils;

public abstract class Constants {

	public static final int CWLEN = 35;
	public static final int PKTSIZE = 1024;
	public static final int BPF = 38016;
	public static final int FPS = 25;
	public static final int WIDTH = 176;
	public static final int HEIGHT = 144;
	public static final long STREAMER_SLEEPTIME = 5;  // 5 ms between packets of the same word

	public static class LOG{
		public static int Silent = 0;
		public static int Verbose = 1;
		public static int Debug = 2;
	}

}
