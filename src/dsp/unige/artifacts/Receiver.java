package dsp.unige.artifacts;

import dsp.unige.OpenRQTest.RxTest;

public class Receiver {

	public static void main(String[] args) {

		if(args.length==0){
			System.out.println("error: syntax is\n java -jar receiver <path>");
			return;
		}
		else
		{
			int path = Integer.parseInt(args[0]);
			RxTest rtest = new RxTest();
			rtest.setPath(path);
			rtest.start();	
			System.out.println("Receiver done, exiting");
			System.exit(0);
		}
	}
}