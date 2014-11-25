package dsp.unige.artifacts;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import jssim.SsimCalculator;
import jssim.SsimException;

public class JpsParser {

	private static final double LENGTH = 30;

	public static void main(String[] args) {

		if(args.length<1)
			usage();

		String working_dir = args[0];
		try {


			FileInputStream receivedFIS = new FileInputStream(new File(working_dir+"/RECEIVED.jps"));
			FileInputStream referenceFIS = new FileInputStream(new File(working_dir+"/REFERENCE.jps"));

			TaggedJpegImage reference = null;
			TaggedJpegImage received = null;

			ArrayList<TimeReferencedSSIM> ssims = new ArrayList<>();

			boolean advance = true;
			

			while(referenceFIS.available()>0){

				reference = TaggedJpegImage.readFromFileInputStream(referenceFIS);
				setProgress(reference.contentId,2000);
				if(advance)
					try {
						received = TaggedJpegImage.readFromFileInputStream(receivedFIS);
					} catch (EOFException e1) {
						received.data = null;
					}

//								System.out.print("REF.:"+reference.contentId+", REC.:"+received.contentId+"\t");
				if(reference.contentId == received.contentId){
					double ssim = 0;
					try {
						 ssim = getSSIM(reference.data , received.data);
					} catch (Exception e) {
					}
					ssims.add(new TimeReferencedSSIM(ssim, received.tstamp));
					advance = true;
//					System.out.print(ssim+" \n");
				}
				else if (reference.contentId < received.contentId){
					ssims.add(new TimeReferencedSSIM(0, -1));
//					System.out.print(" + ");
					advance = false;
				}
				else{
//					System.out.println("Grosso problema.. ref.id>received.id");
					ssims.add(new TimeReferencedSSIM(0, -1));
//					System.out.print(" + ");
				}

			}
			
			writeResults(ssims,working_dir);

		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	private static void setProgress(int progress,int max) {
		int pr = (int) Math.round((double)progress/(double)max*100);
		printProgBar(pr);
	}
	
	public static void printProgBar(int percent){
	    StringBuilder bar = new StringBuilder("[");

	    for(int i = 0; i < 50; i++){
	        if( i < (percent/2)){
	            bar.append("=");
	        }else if( i == (percent/2)){
	            bar.append(">");
	        }else{
	            bar.append(" ");
	        }
	    }

	    bar.append("]   " + percent + "%     ");
	    System.out.print("\r" + bar.toString());
	}

	private static void writeResults(ArrayList<TimeReferencedSSIM> ssims,String working_dir) throws IOException {

		double avg = 0;
		int lost = 0;
		int count = 0;
		
		FileOutputStream rfos = new FileOutputStream(new File(working_dir+"/results.txt"));
		OutputStreamWriter rosw = new OutputStreamWriter(rfos);
		
		FileOutputStream sfos = new FileOutputStream(new File(working_dir+"/ssims.csv"));
		OutputStreamWriter sosw = new OutputStreamWriter(sfos);
		
		for (TimeReferencedSSIM timeReferencedSSIM : ssims) {
			avg += timeReferencedSSIM.ssim;
			sosw.write(timeReferencedSSIM.time +";"+timeReferencedSSIM.ssim+"\n");
			count++;
			if(timeReferencedSSIM.ssim == 0)
				lost++;
		}
		rosw.write("# avg.ssim = "+(String.format("%2.4f",avg/count))+"\n# lost "+lost);
		rosw.flush();
		rosw.close();
		sosw.flush();
		sosw.close();
		System.out.println("\n\nAvg. ssim: "+(String.format("%2.4f",avg/count))+", "+lost+" frames lost");
		
	}

	private static double getSSIM(byte[] fullQuality, byte[] compressed) {
		if(fullQuality == null)
			throw new NullPointerException("fullquality");
		else if(compressed == null)
			throw new NullPointerException("fullquality");
		else{
			SsimCalculator sc;
			try {
				sc = new SsimCalculator(fullQuality);
				return sc.compareTo(compressed); 
			} catch (SsimException | IOException e) {
				e.printStackTrace();
				System.out.println("JpegCoderBenchmark.getSSIM() error calculating SSIM");
			}
			return 0;
		}
	}

	private static void usage() {
		System.out.println("Syntax error. Usage:\njava -jar parser <working_dir>");
		System.exit(1);
	}

	static class TimeReferencedSSIM{
		double ssim;
		long time;

		public TimeReferencedSSIM (double ssim, long time){
			this.ssim = ssim;
			this.time = time;
		}
	}

	static class TaggedJpegImage {
		int contentId;
		byte[] data;
		long tstamp;

		public TaggedJpegImage(int size){
			data = new byte[size];
		}

		public static TaggedJpegImage readFromFileInputStream(FileInputStream fis) throws IOException{

			TaggedJpegImage out;

			/*
			 * 
			dos.writeInt(contentId);
			dos.writeInt(img.length);
			dos.writeLong(System.currentTimeMillis());
			dos.flush();

			fos.write(img);
			fos.flush();
			 */
			DataInputStream dis = new DataInputStream(fis);
			int contentId = dis.readInt();
			int length = dis.readInt();
			long tstamp = dis.readLong();
			out = new TaggedJpegImage(length);
			fis.read(out.data);
			out.contentId = contentId;
			out.tstamp = tstamp;

			return out;

		}
	}
}