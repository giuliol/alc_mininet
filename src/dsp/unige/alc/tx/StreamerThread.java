package dsp.unige.alc.tx;

import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import dsp.unige.alc.utils.CodeWord;
import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Log;
import dsp.unige.alc.utils.Packet;

public class StreamerThread extends Thread{

	CodeWordBuffer cwbHandle;
	DatagramSocket forwardSocket;
	int forwardPort;
	InetAddress destination;
	DatagramPacket forwardPacket;
	boolean RUNNING = true;
	private Writer logWriter;

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
					CodeWord toSend = cwbHandle.get();
					send(toSend);
//					System.out.println("StreamerThread.run() frame "+toSend.pkts[0].data[0] + ", "+toSend.pkts[0].data[1]);
//					System.out.println("-TX- StreamerThread.run() sent to "+forwardPort);
				} catch (IOException e ) {
					e.printStackTrace();
					Log.i(logWriter,"StreamerThread","error sending packet");
				}
			}
			else{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Log.i(logWriter,"StreamerThread","done, terminating");
		sendTerminationPacket();
		cleanUp();
	}

	private void sendTerminationPacket() {
		try {
			forwardPacket.setData(Packet.buildTerminator());
			forwardSocket.send(forwardPacket);
			Log.i(logWriter,"StreamerThread.sendTerminationCodeword()","termination packet sent");
		} catch (IOException e) {
			Log.i(logWriter,"StreamerThread.sendTerminationCodeword()","error sending termination packet");
			e.printStackTrace();
		}
	}

	private void cleanUp() {
		forwardSocket.close();
		Log.i(logWriter,"StreamerThread.cleanUp()","exiting");
	}

	private void send(CodeWord codeWord) throws IOException {
		forwardPacket.setAddress(destination);
		forwardPacket.setPort(forwardPort);

		for(Packet p : codeWord.pkts){
			forwardPacket.setData(p.buildPacket());
			forwardSocket.send(forwardPacket);
			
			try {
				Thread.sleep(Constants.STREAMER_SLEEPTIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.i(logWriter,"StreamerThread","error sleeping");
			}
			
		}
	}

	private void initSocket() {
		try {
			forwardPacket = new DatagramPacket(new byte[Packet.PKTSIZE + Packet.HEADERSIZE], Packet.PKTSIZE + Packet.HEADERSIZE);
			forwardSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			Log.i(logWriter,"StreamerThread","StreamerThread.initSocket() error opening socket");
		}
	}
	
	public void setLogWriter(Writer logWriter) {
		this.logWriter = logWriter;
	}



}
