package dsp.unige.alc.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import dsp.unige.ALC.utils.Constants;
import dsp.unige.OpenRQTest.RQTest;

public class RQCoderBenchmark {

	private static final double CWLEN = Constants.CWLEN;

	public void go() {
		double pl;
		double[] results;

		File csv =  new File("benchmarks/plr_rc.csv");
		OutputStreamWriter osw = null;

		try {
			osw =  new OutputStreamWriter(new FileOutputStream(csv),Charset.forName("UTF-8"));
			String title;
			title = "PL;";
			for(int k=34;k>0;k--){
				title+=String.format("%01.4f",(double)k/CWLEN)+";";
			}
			title+="\n";
			osw.write(title);
			

			for(pl = 0; pl< 1; pl+=0.01){
				osw.write(String.format("%01.6f",pl)+";");
				results = partial(pl);
				for (double d : results) {
					System.out.print(String.format("%01.6f",d)+" ");
					osw.write(String.format("%01.6f",d)+";");
				}
				System.out.println("");
				osw.write("\n");
				osw.flush();
			}

			osw.flush();
			osw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private double[] partial(double pl) {
		RQTest t1 = new RQTest();

		double []errc=new double[34];
		int ITERATIONS = 1000;
		int informationSymbols;

		for(int k=33;k>=0;k--){
			errc[k]=0;
			informationSymbols = k+1;
			for(int i=0;i<ITERATIONS;i++)
				errc[k]+=t1.go(informationSymbols,pl);
			errc[k]=errc[k]/ITERATIONS/CWLEN;
			//			System.out.println("PLoss = "+pl+", Rc="+String.format("%6.2f",((double)k/35))+" errc: "+errc);
		}		
		return errc;
	}




}
