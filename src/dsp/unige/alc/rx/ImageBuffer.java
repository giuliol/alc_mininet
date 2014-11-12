package dsp.unige.alc.rx;

public class ImageBuffer {
	private TaggedImage[] imgs;
	private int head;
	private int tail;
	private int occupancy;
	private int maxSize;
	
	public ImageBuffer(){
		
	}
	
	public void init(int maxSize){
		this.maxSize = maxSize;
		imgs = new TaggedImage[maxSize];
		head = 0;
		tail = 0;
		occupancy = 0;
	}
	
	public synchronized boolean has(int howManyImages){
		return(occupancy + howManyImages <= maxSize);
	}
	
	public synchronized void put(TaggedImage tim){
		imgs[head] = tim;
		head = (head+1)%maxSize;
		occupancy++;
	}
	
	public synchronized TaggedImage get(){
		TaggedImage out = imgs[tail].clone();
		imgs[tail] = null;
		tail=(tail+1)%maxSize;
		occupancy--;
		return out;
	}

	public boolean hasToVisualize() {
//		System.out.println("ImageBuffer.hasToVisualize() occupa "+occupancy);
		return (occupancy >= 1);
	}
}
