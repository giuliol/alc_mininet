package dsp.unige.alc.tx;

import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import dsp.unige.alc.common.Constants;
import dsp.unige.alc.common.Log;

public class FeedbackListenerThread extends Thread {

	CodeWordBuffer cwbHandle;
	Decisor decisor;
	DatagramSocket feedbackSocket;
	//	DatagramSocket socket;
	DatagramPacket reportPacket;
	int feedbackPort;
	ByteBuffer bb;
	int ADAPTIVE = 2;
	SessionParameters sessionParameters;

	public int isADAPTIVE() {
		return ADAPTIVE;
	}

	public void setADAPTIVE(int aDAPTIVE) {
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
				Log.i(logWriter,"ListenerThread.run()","Ready to receive.");
				feedbackSocket.receive(reportPacket);
				
				parse(reportPacket.getData());

			} catch (IOException e) {
				Log.i(logWriter,"ListenerThread.run()","timeout.");
				System.err.print("\n["+System.currentTimeMillis()+"]");
				e.printStackTrace();
				cwbHandle.purge();
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
		int Q = decisor.decideQ(FEC);
		
		if(isADAPTIVE()==0){
			sessionParameters.setQ(Constants.MIN_Q);
			sessionParameters.setFEC(Constants.MAX_FEC);
		}

		if(isADAPTIVE()==1){
			sessionParameters.setFEC(FEC);
			sessionParameters.setQ(Constants.MIN_Q);
		}
		if(isADAPTIVE()==2){
			sessionParameters.setFEC(FEC);
			sessionParameters.setQ(Q);
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
