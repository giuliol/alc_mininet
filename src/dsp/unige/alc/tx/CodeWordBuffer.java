package dsp.unige.alc.tx;

import java.io.Writer;
import java.util.ArrayList;

import dsp.unige.alc.utils.CodeWord;
import dsp.unige.alc.utils.Log;

public class CodeWordBuffer {
	
	ArrayList<CodeWord> buffer;
	int maxSize;
	private Writer logWriter;
	
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
		else{
			Log.i(logWriter,"CodeWordBuffer.put()","WORD DROP");
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

	public synchronized void purge() {
		int toRemove = Math.min(3, buffer.size());
		Log.i(logWriter,"CodeWordBuffer.purge()","dropping "+toRemove);

		for(int i=0;i<toRemove;i++)
			buffer.remove(0);
	}
	
	public void setLogWriter(Writer logWriter) {
		this.logWriter = logWriter;
	}
	
}
