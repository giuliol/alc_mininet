package dsp.unige.alc.common;


public class DoubleVisualizer implements Visualizer {

	DummyVisualizer dv;
	WindowVisualizer wv;
	
	boolean HASWINDOW;
	
	public void init(String path,String name, boolean HEADLESS){
		dv = new DummyVisualizer();
		dv.init(path);
		
		// lays out nicely the windows
		if( !HEADLESS ){
			wv = new WindowVisualizer();
			wv.init(name);
			wv.setOnTheRight();
			HASWINDOW = true;
		}
		
	}
	@Override
	public void display(byte[] img, int contentId) {
		dv.display(img, contentId);
		if(HASWINDOW)
			wv.display(img, contentId);
	}
	
	public void close(){
		dv.close();
		if(HASWINDOW)
			wv.close();
	}
	
	@Override
	public void display(byte[] img, int contentId, String misc) {
		dv.display(img, contentId);
		if(HASWINDOW)
			wv.display(img, contentId , misc);	
		}

}
