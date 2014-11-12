package dsp.unige.alc.tx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class ListenerThread extends Thread {

	CodeWordBuffer cwbHandle;
	Decisor decisor;
	DatagramSocket socket;
	DatagramPacket packet;
	int listeningPort;
	ByteBuffer bb;

	boolean RUNNING = true;
	
	public void stopRunning() {
		RUNNING = false;
	}
	
	public ListenerThread(CodeWordBuffer cwBuffer) {
		cwbHandle = cwBuffer;
	}

	public void setListeningPort(int listeningPort) {
		this.listeningPort = listeningPort;
	}
	
	@Override
	public void run() {
		super.run();
		
		initSocket();
		
		while(RUNNING){
			try {
				socket.receive(packet);
				parse(packet.getData());
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("ListenerThread", "error receiving packet");
			}
		}
		
	}

	private void parse(byte[] data) {
		bb=ByteBuffer.wrap(data);
		
		// acknowledge word
		cwbHandle.ack(bb.getInt());
		// set estimated Rate
		decisor.updateRate(bb.getDouble());
		// set measured Loss
		decisor.updateLoss(bb.getDouble());
		
	}

	private void initSocket() {
		try {
			socket = new DatagramSocket(listeningPort);
			packet = new DatagramPacket((new byte [Packet.PKTSIZE + Packet.HEADERSIZE]), Packet.PKTSIZE + Packet.HEADERSIZE);
		} catch (SocketException e) {
			e.printStackTrace();
			Log.i("ListenerThread","error opening listening datagramSocket");
		}
	}

	public void setDecisor(Decisor decisor) {
		this.decisor = decisor;
	}
}
