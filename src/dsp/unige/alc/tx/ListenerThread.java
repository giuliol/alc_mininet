package dsp.unige.alc.tx;

import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Log;

public class ListenerThread extends Thread {

	CodeWordBuffer cwbHandle;
	Decisor decisor;
	DatagramSocket socket;
	DatagramPacket packet;
	int backwardPort;
	ByteBuffer bb;
	SessionParameters sessionParameters;

	boolean RUNNING = true;
	private Writer logWriter;

	public void stopRunning() {
		RUNNING = false;
	}

	public ListenerThread(CodeWordBuffer cwBuffer,
			SessionParameters sessionParameters) {
		cwbHandle = cwBuffer;
		this.sessionParameters = sessionParameters;
	}

	public void setBackwardPort(int listeningPort) {
		this.backwardPort = listeningPort;
	}

	@Override
	public void run() {
		super.run();

		initSocket();

		while (RUNNING) {
			try {
				socket.receive(packet);
				parse(packet.getData());
			} catch (IOException e) {
				e.printStackTrace();
				Log.i(logWriter,"ListenerThread", "socket timeout");
				cwbHandle.purge();
			}
		}

		cleanUp();

	}

	private void cleanUp() {
		socket.close();
	}

	private void parse(byte[] data) {
		bb = ByteBuffer.wrap(data);

		int codeWordNumber = bb.getInt();
		double estimatedRate = bb.getDouble();
		int measuredLoss = bb.getInt();

		// acknowledge word -> delete it from cwbuffer
		cwbHandle.ack(codeWordNumber);
		// set estimated Rate
		decisor.updateRate(estimatedRate);
		// set measured Loss
		decisor.updateLoss(measuredLoss);

		Log.i(logWriter,"ListenerThread.parse()","received report for "
				+ codeWordNumber
				+ " "
				+ String.format("codew. %d, est. rate: %6.2f, lost: %d",
						codeWordNumber, estimatedRate, measuredLoss));

		int FEC = decisor.decideFEC();
		int Q = decisor.decideQ((Constants.CWLEN - (double) FEC)
				/ (double) Constants.CWLEN, FEC);

		Log.i(logWriter,"ListenerThread.parse()","decided Q=" + Q + ", FEC=" + FEC);

		sessionParameters.setQ(Q);
		sessionParameters.setFEC(FEC);

	}

	private void initSocket() {
		try {
			socket = new DatagramSocket(backwardPort);
			packet = new DatagramPacket(new byte[Constants.FEEDBACK_PKTSIZE],
					Constants.FEEDBACK_PKTSIZE);
			socket.setSoTimeout(2500);
		} catch (SocketException e) {
			e.printStackTrace();
			Log.i(logWriter,"ListenerThread", "error opening listening datagramSocket");
		}
	}

	public void setLogWriter(Writer logWriter) {
		this.logWriter = logWriter;
	}
	
	public void setDecisor(Decisor decisor) {
		this.decisor = decisor;
	}
}
