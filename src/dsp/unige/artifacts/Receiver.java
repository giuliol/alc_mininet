package dsp.unige.artifacts;

import dsp.unige.OpenRQTest.RxTest;

public class Receiver {

	public static void main(String[] args) {

		if(args.length<2){
			System.out.println("error: syntax is\n java -jar receiver <path> <headless>\npath is a number, headless 1 for true, 0 for false");
			return;
		}
		else
		{
			int path = Integer.parseInt(args[0]);
			boolean HEADLESS;
			if(args[1].equals("0"))
				HEADLESS = false;
			else
				HEADLESS = true;
			
			
			
			RxTest rtest = new RxTest();
			rtest.setPath(path);
			rtest.start(HEADLESS);	
			System.out.println("Receiver done, exiting");
			System.exit(0);
		}
	}
}