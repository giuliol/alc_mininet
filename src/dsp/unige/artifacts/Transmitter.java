package dsp.unige.artifacts;

import dsp.unige.OpenRQTest.TxTest;

public class Transmitter {

	public static void main(String[] args) {
		if(args.length<2){
			System.out.println("error: syntax is\n java -jar transmitter <destination_IP> <path>");
			return;
		}
		else
		{
			String dest = args[0];
			int path = Integer.parseInt(args[1]);
			TxTest ttest = new TxTest();
			ttest.setPath(path);
			ttest.goWithDummyScreen(dest);
			System.out.println("Transmitter done, exiting");
		}
	}
}
