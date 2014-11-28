package dsp.unige.alc.tx;

import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Log;

public class FeedbackListenerThread extends Thread {

	CodeWordBuffer cwbHandle;
	Decisor decisor;
	DatagramSocket feedbackSocket;
	//	DatagramSocket socket;
	DatagramPacket reportPacket;
	int feedbackPort;
	ByteBuffer bb;
	boolean ADAPTIVE = true;
	SessionParameters sessionParameters;

	public boolean isADAPTIVE() {
		return ADAPTIVE;
	}

	public void setADAPTIVE(boolean aDAPTIVE) {
		ADAPTIVE = aDAPTIVE;
	}

	boolean RUNNING = true;
	private Writer logWriter;

	public void stopRunning() {
		RUNNING = false;
	}

	public FeedbackListenerThread(CodeWordBuffer cwBuffer,
			SessionParameters sessionParameters) {
		cwbHandle = cwBuffer;
		this.sessionParameters = sessionParameters;
	}

	public void setBackwardPort(int listeningPort) {
		this.feedbackPort = listeningPort;
	}

	@Override
	public void run() {
		super.run();

		initSocket();

		while (RUNNING) {

			try {

				feedbackSocket.receive(reportPacket);
				parse(reportPacket.getData());

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		cleanUp();

	}

	private void cleanUp() {

		feedbackSocket.close();
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
		int Q = decisor.decideQ( FEC);


		if(isADAPTIVE()){
			sessionParameters.setQ(Q);
			sessionParameters.setFEC(FEC);
		}
	}

	private void initSocket() {
		try {
			feedbackSocket = new DatagramSocket(feedbackPort);
			reportPacket = new DatagramPacket(new byte[Constants.FEEDBACK_PKTSIZE] ,Constants.FEEDBACK_PKTSIZE);
			feedbackSocket.setSoTimeout(Constants.FEEDBACK_SOCKET_TIMEOUT);
		} catch (SocketException e) {
			Log.i(logWriter,"ListenerThread", "error opening listening datagramSocket");
			e.printStackTrace();
		}
	}

	public void setLogWriter(Writer logWriter) {
		this.logWriter = logWriter;
	}

	public void setDecisor(Decisor decisor) {
		this.decisor = decisor;
	}
}
