package dsp.unige.artifacts;

import dsp.unige.OpenRQTest.TxTest;

public class Transmitter {

	public static void main(String[] args) {
		if(args.length<3){
			System.out.println("error: syntax is\n java -jar transmitter <destination_IP> <path> <adaptive>");
			return;
		}
		else
		{
			String dest = args[0];
			int path = Integer.parseInt(args[1]);
			boolean adaptive = Integer.parseInt(args[2]) == 1 ;
			TxTest ttest = new TxTest();
			ttest.setPath(path);
			ttest.start(dest,adaptive);
			System.out.println("Transmitter done, exiting");
			System.exit(0);
		}
	}
}
