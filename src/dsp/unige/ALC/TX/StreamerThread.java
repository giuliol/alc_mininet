package dsp.unige.ALC.TX;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketException;

public class StreamerThread extends Thread{

	CodeWordBuffer cwbHandle;
	DatagramSocket socket;
	int destinationPort;
	Inet4Address destination;
	DatagramPacket packet;
	
	
	public StreamerThread(CodeWordBuffer cwBuffer) {
		cwbHandle = cwBuffer;
	}
	
	public void setDestination( Inet4Address destination, int destinationPort){
		this.destination = destination;
		this.destinationPort =  destinationPort;
	}
	
	@Override
	public void run() {
		super.run();
		initSocket(); 
		
		if(cwbHandle.hasAvailable()){
			try {
				send(cwbHandle.get());
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("StreamerThread","error sending packet");
			}
		}
	}

	private void send(CodeWord codeWord) throws IOException {
		packet.setAddress(destination);
		packet.setPort(destinationPort);
		
		for(Packet p : codeWord.pkts){
			packet.setData(p.buildPacket());
			socket.send(packet);
		}
	}

	private void initSocket() {
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			Log.i("StreamerThread","StreamerThread.initSocket() error opening socket");
		}
	}

	

}
