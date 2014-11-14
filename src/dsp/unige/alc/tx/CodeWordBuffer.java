package dsp.unige.alc.tx;

import java.util.ArrayList;

import dsp.unige.alc.utils.CodeWord;

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
		System.out.println("CodeWordBuffer.put() OCCUPANCY " +((int)(100*buffer.size()/(double)maxSize)));
		if(buffer.size() < maxSize){
			buffer.add(word);
		}
		else{
			System.out.println("CodeWordBuffer.ack() WORD DROP");
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

	public void purge() {
		int toRemove = Math.min(3, buffer.size());
		System.out.println("CodeWordBuffer.purge() dropping "+toRemove);

		for(int i=0;i<toRemove;i++)
			buffer.remove(0);
	}
	
}
