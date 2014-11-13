package dsp.unige.ALC.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DummyVisualizer implements Visualizer {

	FileOutputStream fos;
	DataOutputStream dos;
	long lastFrameTime;
	private long interFrameTime = (long) 1000/fps ;
	
	public void init(String path){
		File out =  new File(path);
		try {
			fos = new FileOutputStream(out);
			dos = new DataOutputStream(fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.i("DummyVisualizer","error opening file");
		}
	}
	
	@Override
	public void display(byte[] img, int contentId) {
		try {
			long wakeUp = lastFrameTime + interFrameTime ;
			long toSleep;
			long now = System.currentTimeMillis();
			if(wakeUp < now)
				toSleep = 0;
			else 
				toSleep = wakeUp - now;
			
			Thread.sleep(toSleep);
			
			dos.writeInt(contentId);
			dos.writeInt(img.length);
			dos.writeLong(System.currentTimeMillis());
			dos.flush();
			
			fos.write(img);
			fos.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("DummyVisualizer","error writing to file");
		} catch (InterruptedException e) {
			Log.i("DummyVisualizer","error sleeping");
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			dos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("DummyVisualizer","error closing the stream");
		}

	}

}
