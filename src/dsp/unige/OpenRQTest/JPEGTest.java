package dsp.unige.OpenRQTest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import dsp.unige.ALC.utils.JpegCore;
import dsp.unige.ALC.utils.JpegEncoder;
import dsp.unige.ALC.utils.ReadYUV;

public class JPEGTest {

	public void go()  {

		int bytesPerImage = 38016 ;
		FakeCamera fc =  new FakeCamera();
		fc.setBpf(bytesPerImage);
		JpegCore jpenc;
		BufferedImage bim;
		ByteArrayOutputStream baos = null;
		int Q = 150;
		byte[] jpeg;
		
		try {
			
			baos = new ByteArrayOutputStream();
			ReadYUV ryuv = new ReadYUV(176, 144); 
			ryuv.startReading("video/highway_qcif.yuv");
			
			bim = ryuv.nextImageYOnly();
			while(bim != null){
				byte[] data = JpegEncoder.Compress(bim, Q);
//				System.out.println("JPEGTest.go() len "+data.length);
				
				
				
				bim = ryuv.nextImageYOnly();
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
