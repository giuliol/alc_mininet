package dsp.unige.alc.utils;


public class DoubleVisualizer implements Visualizer {

	DummyVisualizer dv;
	ActualVisualizer av;
	
	public void init(String path,String name){
		dv = new DummyVisualizer();
		av = new ActualVisualizer();
		
		dv.init(path);
		av.init(name);
		
	}
	@Override
	public void display(byte[] img, int contentId) {
		dv.display(img, contentId);
		av.display(img, contentId);
	}
	
	public void close(){
		dv.close();
		av.close();
	}

}
