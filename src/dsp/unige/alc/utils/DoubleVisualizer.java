package dsp.unige.alc.utils;


public class DoubleVisualizer implements Visualizer {

	DummyVisualizer dv;
	ActualVisualizer av;
	
	boolean HASWINDOW;
	
	public void init(String path,String name){
		dv = new DummyVisualizer();
		dv.init(path);
		
		// lays out nicely the windows
		if(name.contains("RECEIVER") || name.contains("receiver")){
			av = new ActualVisualizer();
			av.init(name);
			av.setOnTheRight();
			HASWINDOW = true;
		}
		
	}
	@Override
	public void display(byte[] img, int contentId) {
		dv.display(img, contentId);
		if(HASWINDOW)
			av.display(img, contentId);
	}
	
	public void close(){
		dv.close();
		if(HASWINDOW)
			av.close();
	}

}
