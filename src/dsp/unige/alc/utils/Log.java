package dsp.unige.alc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

	public static void i(String tag, String msg){
		long time = System.currentTimeMillis();
		String text = new SimpleDateFormat("hh:mm:ss.SSS").format(new Date(time));

		System.out.println("["+text+"] "+tag + " " + msg);
	}
}
