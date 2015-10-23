package dsp.unige.artifacts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import dsp.unige.OpenRQTest.TxTest;

public class Transmitter {

	public static void main(String[] args) {
		if(args.length<3){
			System.out.println("error: syntax is\n java -jar transmitter <destination_IP> <path> <adaptive>\n "
					+ "<adaptive> = 0 fully static\n"
					+ "<adaptive> = 1 adaptive FEC\n"
					+ "<adaptive> = 2 fully adaptive");
			return;
		}
		else
		{
			String dest = args[0];
			int path = Integer.parseInt(args[1]);
			int adaptive = Integer.parseInt(args[2])  ;
			TxTest ttest = new TxTest();
			ttest.setPath(path);
			
			
			try {
				PrintStream ps;
				ps = new PrintStream(path+"/T"+path+"_errors.log");
				System.setErr(ps);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			ttest.start(dest,adaptive);
			System.out.println("Transmitter done, exiting");
			System.exit(0);
		}
	}
	
	class myPrintStream extends PrintStream {

		public myPrintStream(File file) throws FileNotFoundException {
			super(file);
		}
		
		@Override
		public void print(String s) {
			String ss = System.currentTimeMillis() + s;
			super.print(ss);
		}
		
	}
}
