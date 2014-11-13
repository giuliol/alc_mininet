package dsp.unige.alc.tx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import dsp.unige.ALC.utils.CodeWord;
import dsp.unige.ALC.utils.Constants;
import dsp.unige.ALC.utils.Log;
import dsp.unige.ALC.utils.Packet;

public class StreamerThread extends Thread{

	CodeWordBuffer cwbHandle;
	DatagramSocket socket;
	int forwardPort;
	InetAddress destination;
	DatagramPacket packet;
	boolean RUNNING = true;

	public void stopRunning(){
		RUNNING = false;
	}

	public StreamerThread(CodeWordBuffer cwBuffer) {
		cwbHandle = cwBuffer;
	}

	public void setDestination( InetAddress destination, int destinationPort){
		this.destination = destination;
		this.forwardPort =  destinationPort;
	}

	@Override
	public void run() {
		super.run();
		initSocket(); 

		while(RUNNING){
			if(cwbHandle.hasAvailable()){
				try {
					Thread.sleep(Constants.STREAMER_SLEEPTIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.i("StreamerThread","error sleeping");
				}

				try {
					CodeWord toSend = cwbHandle.get();
					send(toSend);
//					System.out.println("StreamerThread.run() frame "+toSend.pkts[0].data[0] + ", "+toSend.pkts[0].data[1]);
//					System.out.println("-TX- StreamerThread.run() sent to "+forwardPort);
				} catch (IOException e) {
					e.printStackTrace();
					Log.i("StreamerThread","error sending packet");
				}
			}
		}
		
		cleanUp();
	}

	private void cleanUp() {
		socket.close();
		System.out.println("StreamerThread.cleanUp() exiting");
	}

	private void send(CodeWord codeWord) throws IOException {
		packet.setAddress(destination);
		packet.setPort(forwardPort);

		for(Packet p : codeWord.pkts){
			packet.setData(p.buildPacket());
			socket.send(packet);
		}
	}

	private void initSocket() {
		try {
			packet = new DatagramPacket(new byte[Packet.PKTSIZE + Packet.HEADERSIZE], Packet.PKTSIZE + Packet.HEADERSIZE);
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			Log.i("StreamerThread","StreamerThread.initSocket() error opening socket");
		}
	}



}
