package dsp.unige.ALC.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

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
	
	public static byte[] Compress(byte[] img, int Q){
		
		baos = new ByteArrayOutputStream();
		BufferedImage bim = null;
		try {
			bim = ImageIO.read(new ByteArrayInputStream(img));
		} catch (IOException e1) {
			System.out.println("JpegEncoder.Compress() error creating bytearrayinputstream from byte array");
			e1.printStackTrace();
			return null;
		}
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
