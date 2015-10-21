package dsp.unige.alc.common;

public interface Visualizer {

	int fps = Constants.FPS;
	public void display(byte[] img, int contentId);
	public void display(byte[] img, int contentId, String misc);
}
