package dsp.unige.OpenRQTest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import dsp.unige.alc.utils.JpegEncoder;
import dsp.unige.alc.utils.ReadYUV;

public class JPEGTest {

	public void go()  {

		BufferedImage bim;
		ByteArrayOutputStream baos = null;
		int Q = 150;
		
		try {
			
			baos = new ByteArrayOutputStream();
			ReadYUV ryuv = new ReadYUV(176, 144); 
			ryuv.startReading("video/highway_qcif.yuv");
			
			bim = ryuv.nextImageYOnly();
			int j=0;
			while(bim != null && j<10){
				byte[] data = JpegEncoder.Compress(bim, Q);
//				System.out.println("JPEGTest.go() len "+data.length);
				bim = ryuv.nextImageYOnly();
				j++;
			}

			baos.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
				
//		try {
//			fc.load("/video/sequence.raw");
//
//			while(fc.hasFrame()){
//				byte[] data = fc.getFrame();
//				bim = 
////				image = new Im
//			}
//
//		}catch(Exception e){
//
//		}
	}

}
