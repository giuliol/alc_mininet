package dsp.unige.artifacts;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import jssim.SsimCalculator;
import jssim.SsimException;

public class JpsParser {

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
				if(advance)
					received = TaggedJpegImage.readFromFileInputStream(receivedFIS);

				if(reference.contentId == received.contentId){
					double ssim = getSSIM(reference.data , received.data);
					ssims.add(new TimeReferencedSSIM(ssim, received.tstamp));
					advance = true;
				}
				else if (reference.contentId < received.contentId){
					ssims.add(new TimeReferencedSSIM(0, -1));
					System.out.println("Lost frame "+reference.contentId);
				}
				else{
					System.out.println("Grosso problema.. ref.id>received.id");
				}

			}


		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	private static double getSSIM(byte[] fullQuality, byte[] compressed) {
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

	private static void usage() {
		System.out.println("Syntax error. Usage:\njava -jar parser <working_dir>");
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