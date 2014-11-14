package dsp.unige.alc.rx;

import dsp.unige.alc.utils.Constants;

public class ImageBuffer {
	private static final long MAX_INTERFRAME_TIME = Constants.MAX_INTERFRAME_TIME;
	private TaggedImage[] imgs;
	private int head;
	private int tail;
	private int occupancy;
	private int maxSize;
	private int receivedFramesInWord;
	private long timeStamp;
	
	
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

	public synchronized void setReceived(int rcv){
		receivedFramesInWord = rcv;
		timeStamp = System.currentTimeMillis() - timeStamp;

	}
	
	public synchronized long getInterFrameTime() {
		return Math.min(Math.round((double)timeStamp / receivedFramesInWord) , MAX_INTERFRAME_TIME ) ;
	}
	public boolean hasToVisualize() {
//		System.out.println("ImageBuffer.hasToVisualize() occupa "+occupancy);
		return (occupancy >= 1);
	}
}
