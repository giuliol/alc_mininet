package dsp.unige.OpenRQTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FastFileWriter extends FileWriter {

	char[] buffer =  new char[800000];
	int pos = 0;
	
	public FastFileWriter(File file) throws IOException {
		super(file);
	}
	@Override
	public void write(String str) throws IOException {
		str.getChars(0, str.length(), buffer, pos);
		pos +=str.length();
	}
	
	@Override
	public void close() throws IOException {
		super.write(buffer,0,pos);
		super.close();
	}
}
