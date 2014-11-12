package dsp.unige.ALC.utils;

public interface Visualizer {

	int fps = Constants.FPS;
	public void display(byte[] img, int contentId);
}
