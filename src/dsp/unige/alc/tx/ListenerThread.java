package dsp.unige.alc.tx;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Log;

public class ListenerThread extends Thread {

	CodeWordBuffer cwbHandle;
	Decisor decisor;
	ServerSocket feedbackServerSocket;
	Socket feedbackSocket;
	DataInputStream feedbackReader;
	//	DatagramSocket socket;
	//	DatagramPacket packet;
	int feedbackPort;
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
		this.feedbackPort = listeningPort;
	}

	@Override
	public void run() {
		super.run();

		initSocket();

		while (RUNNING) {
			try {
				while(!feedbackSocket.isConnected()){

				}
				boolean feedback = feedbackReader.readBoolean();
				if(feedback){

					int codeWordNumber = feedbackReader.readInt();
					double estimatedRate = feedbackReader.readDouble();
					int measuredLoss = feedbackReader.readInt();
					long interCodeWordTime = feedbackReader.readLong();


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

					Log.i(logWriter,"ListenerThread.parse()","decided Q=" + Q + ", FEC=" + FEC);

					sessionParameters.setQ(Q);
					sessionParameters.setFEC(FEC);
				}
				else{
					System.out.println("\nListenerThread.run() timeout, purging\n");
					cwbHandle.purge();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		cleanUp();

	}

	private void cleanUp() {

		try {
			feedbackReader.close();
			feedbackSocket.close();
			feedbackServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		Log.i(logWriter,"ListenerThread.parse()","decided Q=" + Q + ", FEC=" + FEC);

		sessionParameters.setQ(Q);
		sessionParameters.setFEC(FEC);

	}

	private void initSocket() {
		//		try {
		//			socket = new DatagramSocket(backwardPort);
		//			packet = new DatagramPacket(new byte[Constants.FEEDBACK_PKTSIZE],
		//					Constants.FEEDBACK_PKTSIZE);
		//			socket.setSoTimeout(2500);
		//		} catch (SocketException e) {
		//			e.printStackTrace();
		//			Log.i(logWriter,"ListenerThread", "error opening listening datagramSocket");
		//		}
		try {
			feedbackServerSocket = new ServerSocket(feedbackPort);
			feedbackSocket = feedbackServerSocket.accept();
			feedbackReader = new DataInputStream(feedbackSocket.getInputStream());
		} catch (IOException e) {
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
