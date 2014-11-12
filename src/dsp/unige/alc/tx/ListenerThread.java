package dsp.unige.alc.tx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import dsp.unige.ALC.utils.Packet;

public class ListenerThread extends Thread {

	CodeWordBuffer cwbHandle;
	Decisor decisor;
	DatagramSocket socket;
	DatagramPacket packet;
	int listeningPort;
	ByteBuffer bb;
	SessionParameters sessionParameters;

	boolean RUNNING = true;
	
	public void stopRunning() {
		RUNNING = false;
	}
	
	public ListenerThread(CodeWordBuffer cwBuffer, SessionParameters sessionParameters) {
		cwbHandle = cwBuffer;
		this.sessionParameters = sessionParameters;
	}

	public void setBackwardPort(int listeningPort) {
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
		
		cleanUp();
		
	}

	private void cleanUp() {
		socket.close();
	}

	private void parse(byte[] data) {
		bb=ByteBuffer.wrap(data);
		
		int codeWordNumber=bb.getInt();
		double estimatedRate=bb.getDouble();
		int measuredLoss=bb.getInt();
		
		System.out.println("ListenerThread.parse() "+String.format("codew. %d, est. rate: %6.2f, lost: %d",codeWordNumber,estimatedRate,measuredLoss));
		// acknowledge word -> delete it from cwbuffer
		cwbHandle.ack(codeWordNumber);
		// set estimated Rate
		decisor.updateRate(estimatedRate);
		// set measured Loss
		decisor.updateLoss(measuredLoss);
		
		sessionParameters.setQ(decisor.decideQ());
		sessionParameters.setFEC(decisor.decideFEC());

	}

	private void initSocket() {
		try {
			socket = new DatagramSocket(listeningPort);
			packet = new DatagramPacket((new byte [Packet.PKTSIZE + Packet.HEADERSIZE]), Packet.PKTSIZE + Packet.HEADERSIZE);
			System.out.println("-TX- ListenerThread.initSocket() listening on "+listeningPort);
		} catch (SocketException e) {
			e.printStackTrace();
			Log.i("ListenerThread","error opening listening datagramSocket");
		}
	}

	public void setDecisor(Decisor decisor) {
		this.decisor = decisor;
	}
}
