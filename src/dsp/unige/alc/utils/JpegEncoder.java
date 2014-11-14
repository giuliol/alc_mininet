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
	
	public static byte[] Compress(byte[] img, int Q,int width, int height){
		
		baos = new ByteArrayOutputStream();
		BufferedImage bim = fromByteArray(img,width,height);
//
//		try {
//			bim = ImageIO.read(new ByteArrayInputStream(img));
//		} catch (IOException e1) {
//			System.out.println("JpegEncoder.Compress() error creating bytearrayinputstream from byte array");
//			e1.printStackTrace();
//			return null;
//		}
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

	private static BufferedImage fromByteArray(byte[] oneFrame,int width, int height) {
        try
        {

                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                
                for (int j = 0; j < height; j++)
                {
                        for (int i = 0; i < width; i++)
                        {
                                int rColor = getRGBFromStream(i, j,width,height,oneFrame);
                                image.setRGB(i, j, rColor);
                        }
                }

                return image;
        } 
        
        catch (Exception e)
        {
                e.printStackTrace();
        }
        return null;
	}

	private static int getRGBFromStream(int x, int y, int width, int height,byte[] oneFrame) {
                int arraySize = height * width;
                int Y = unsignedByteToInt(oneFrame[y * width + x]);
                int U = unsignedByteToInt(oneFrame[(y/2) * (width/2) + x/2 + arraySize]);
                int V = unsignedByteToInt(oneFrame[(y/2) * (width/2) + x/2 + arraySize + arraySize/4]);

                int R = (int)(Y + 1.370705 * (V-128));
                int G = (int)(Y - 0.698001 * (V-128) - 0.337633 * (U-128));
                int B = (int)(Y + 1.732446 * (U-128));

                if(R>255) R = 255;
                if(G>255) G = 255;
                if(B>255) B = 255;
                
                if(R<0) R = 0;
                if(G<0) G = 0;
                if(B<0) B = 0;
                
                int rColor = (0xff << 24) | (R << 16) | (G << 8) | B;

                return rColor;
	}
	
    private static int unsignedByteToInt(byte b)
    {
            return (int) b & 0xFF;
    }
}
