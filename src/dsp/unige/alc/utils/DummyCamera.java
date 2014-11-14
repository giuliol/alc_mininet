package dsp.unige.alc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DummyCamera implements Camera{

	String videoFile;
	FileInputStream fis;
	long lastFrameTime;
	long interFrameTime;
	int bpf;
	int totalFrames;

	public int getTotalFrames() {
		return totalFrames;
	}
	
	public void init(String videoFile, int bpf, int fps){
		this.videoFile = videoFile;
		this.bpf = bpf;
		lastFrameTime = System.currentTimeMillis();
		interFrameTime = Math.round(1000d / fps);
	}

	@Override
	public void open() {
		try {
			fis = new FileInputStream(new File(videoFile));
			totalFrames = fis.available() / bpf;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("DummyCamera.open() error opening "+videoFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("DummyCamera.open() error computing available bytes for "+videoFile);

		}
	}

	@Override
	public byte[] getFrame() {
		waitInterFrameTime();
		byte [] frame = new byte [bpf];
		try {
			fis.read(frame);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("DummyCamera.getFrame() error reading frame");
		}
		lastFrameTime = System.currentTimeMillis();
		return frame;
	}

	private void waitInterFrameTime() {
		long wakeUp = lastFrameTime + interFrameTime;
		long now = System.currentTimeMillis();
		long toSleep;
		
		if(wakeUp > now)
			toSleep = wakeUp - now;
		else{
			System.out.println("DummyCamera.waitInterFrameTime() warning: "+ (now - wakeUp) + " seconds late.");
			toSleep = 0;
		}
		try {
			Thread.sleep(toSleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("DummyCamera.waitInterFrameTime() error sleeping");
		}		
		
	}

	@Override
	public void close() {
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("DummyCamera.close() error closing fileinputstream");
		}
	}

	@Override
	public boolean hasFrame(){
		try {
			return (fis.available() >= bpf );
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}



}
