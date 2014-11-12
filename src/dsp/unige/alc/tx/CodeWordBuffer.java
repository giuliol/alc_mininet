package dsp.unige.alc.tx;

import java.util.ArrayList;

public class CodeWordBuffer {
	
	ArrayList<CodeWord> buffer;
	int maxSize;
	
	public CodeWordBuffer(){
		
	}
	
	public void init(int cwbsize) {
		buffer =  new ArrayList<CodeWord>();
		maxSize = cwbsize;
	}

	public synchronized void put(CodeWord word) {
		if(buffer.size() < maxSize){
			buffer.add(word);
		}
	}
	
	public synchronized boolean ack(int cwNo){
		for(int i=0;i<buffer.size();i++){
			if(buffer.get(i).codeWordNumber == cwNo){
				buffer.remove(i);
				return true;
			}
		}
		return false;
	}

	public synchronized CodeWord get(){
		for (CodeWord cw : buffer) {
			if(cw.available){
				cw.available = false;
				return cw;
			}
		}
		return null;
	}
	
	public synchronized boolean hasAvailable() {
		for (CodeWord cw : buffer) {
			if(cw.available)
				return true;
		}
		return false;
	}
	
}
