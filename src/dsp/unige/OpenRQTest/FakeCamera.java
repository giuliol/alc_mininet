package dsp.unige.OpenRQTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FakeCamera {

	private FileInputStream fis;
	private int bpf;
	
	public void setBpf(int bpf) {
		this.bpf = bpf;
	}
	
	public int getBpf() {
		return bpf;
	}
	
	public void load(String string) throws FileNotFoundException {
		fis = new FileInputStream(new File(string));
		
	}
	
	public boolean hasFrame(){
		try {
			return (fis.available() >= bpf );
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public byte[] getFrame() throws IOException {
		byte [] frame = new byte [bpf];
		fis.read(frame);
		return frame;
	}
	

}
