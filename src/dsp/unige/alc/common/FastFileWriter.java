package dsp.unige.alc.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FastFileWriter extends FileWriter {

	char[] buffer =  new char[1];
	int pos = 0;
	
	public FastFileWriter(File file) throws IOException {
		super(file);
	}
	@Override
	public void write(String str) throws IOException {
//		str.getChars(0, str.length(), buffer, pos);
//		pos +=str.length();
		super.write(str);
		super.flush();
		
	}
	
	@Override
	public void close() throws IOException {
//		super.write(buffer,0,pos);
		super.flush();
		super.close();
	}
}
