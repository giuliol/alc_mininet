package dsp.unige.alc.benchmark;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import jssim.SsimCalculator;
import jssim.SsimException;
import dsp.unige.ALC.utils.Constants;
import dsp.unige.ALC.utils.DummyCamera;
import dsp.unige.ALC.utils.JpegEncoder;

public class JpegCoderBenchmark {


	public HashMap<String, Double> test(String sequence,int Q){

		DummyCamera dc = new DummyCamera();
		byte[] compressed;
		byte[] fullQuality;

		dc.init(sequence, Constants.BPF, Constants.FPS);
		dc.open();

		double avgSize = 0;
		double avgSSIM = 0;
		int count = 0;
		int totFrames = dc.getTotalFrames();
		byte[] frame;

		while(dc.hasFrame()){
			count++;
			frame = dc.getFrame();
			compressed = JpegEncoder.Compress(frame, Q, Constants.WIDTH, Constants.HEIGHT);
			fullQuality = JpegEncoder.Compress(frame, 100, Constants.WIDTH, Constants.HEIGHT);

			avgSize += compressed.length;
			avgSSIM += getSSIM(compressed,fullQuality);

			if(count%100==0){
				System.out.println(String.format("%6.2f percent",((double)count / totFrames)*100d));
			}
		}

		System.out.println("");
		dc.close();

		avgSize = avgSize / count;
		avgSSIM = avgSSIM / count;

		HashMap<String, Double> out = new HashMap<>();
		out.put("Average SSIM", avgSSIM);
		out.put("Average Size", avgSize);

		return out;

	}

	private double getSSIM(byte[] compressed, byte[] fullQuality) {

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

	public HashMap<String, Double> testAll(int Q) {
		double avgSize;
		double avgSSIM;
		double [] results = new double[2];
		int count = 0;

		File[] files = new File("benchmarks/video_benchmark").listFiles();
		for (File file : files) {
			count += testMany(file, Q, results);
		}

		avgSize = results[0] / count;
		avgSSIM = results[1] / count;

		HashMap<String, Double> out = new HashMap<>();
		out.put("Q", (double) Q);
		out.put("AvgSize", avgSSIM);
		out.put("AvgSSIM", avgSize);

		return out;
	}

	private int testMany(File file, int Q, double[] results) {
		byte[] compressed;
		byte[] fullQuality;
		byte[] frame;

		DummyCamera dc = new DummyCamera();

		System.out.println("Working on "+file.getAbsolutePath());
		
		dc.init(file.getAbsolutePath(), Constants.BPF, Constants.FPS);
		dc.open();
		
		int count = 0;
		
		while(dc.hasFrame()){
			frame = dc.getFrame();
			compressed = JpegEncoder.Compress(frame, Q, Constants.WIDTH, Constants.HEIGHT);
			fullQuality = JpegEncoder.Compress(frame, 100, Constants.WIDTH, Constants.HEIGHT);

			results[0] += compressed.length;
			results[1] += getSSIM(compressed,fullQuality);
			
			count++;
		}
		
		dc.close();
		return count;
	}
}
