package dsp.unige.alc.utils;

public interface Camera {

	public void open();
	public byte[] getFrame();
	public void close();
	public boolean hasFrame();
	
}
