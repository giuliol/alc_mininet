package dsp.unige.ALC.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class JpegEncoder {

	static JpegCore jpenc;
	private static ByteArrayOutputStream baos;
	
	public static byte[] Compress(BufferedImage bim, int Q){
		
		baos = new ByteArrayOutputStream();
		jpenc =  new JpegCore(bim, Q, baos);
		jpenc.Compress();
		try {
			baos.flush();
		} catch (IOException e) {
			System.out.println("JpegEncoder.Compress() error flushing ByteArrayOutputStream");
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
}
