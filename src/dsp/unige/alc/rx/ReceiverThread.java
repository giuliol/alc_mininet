package dsp.unige.alc.rx;

import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.fec.openrq.parameters.FECParameters;
import dsp.unige.alc.common.Constants;
import dsp.unige.alc.common.Log;
import dsp.unige.alc.common.Packet;
import dsp.unige.alc.common.RQDecoder;

public class ReceiverThread extends Thread {

	private static final int SO_TIMEOUT = 2000;
	private DatagramSocket socket;
	private DatagramSocket feedbackSocket;
	private DatagramPacket networkPacket;
	private DatagramPacket reportPacket;
	private int forwardPort;
	private RQDecoder decoder;
	private boolean RUNNING = true;
	private ImageBuffer imageBuffer;
	private int currentCodeWordNumber;
	private ArrayList<Packet> packetBuffer;
	private int firstSequenceNumberReceived;
	private int lastSequenceNumberReceived;
	private long firstSequenceNumberReceivedTime;
	private long lastSequenceNumberReceivedTime;
	private boolean[] checkList;
	private int feedbackPort;
	private long lastCodeWordTime;
	private Writer logWriter;
	private boolean FIRST_PACKET;
	private ByteBuffer bb;
	private String info;
	

	public void setLogWriter(Writer logWriter) {
		this.logWriter = logWriter;
	}

	public void setBackwardPort(int listeningPort) {
		this.feedbackPort = listeningPort;
	}


	public ReceiverThread(ImageBuffer imageBuffer, RQDecoder dec) {
		this.decoder = dec;
		this.imageBuffer = imageBuffer;
	}

	private void initSocket(){
		try {
			socket = new DatagramSocket(forwardPort);
		} catch (SocketException e) {
			e.printStackTrace();
			Log.i(logWriter,"ReceiverThread", "error opening socket");
		}
		networkPacket = new DatagramPacket(new byte[Packet.PKTSIZE + Packet.HEADERSIZE + RQDecoder.HEADERSIZE], Packet.PKTSIZE + Packet.HEADERSIZE + RQDecoder.HEADERSIZE);
	}
	

	private void initFeedbackSocket(DatagramPacket networkPacket2) {
		try {
			feedbackSocket = new DatagramSocket();
			reportPacket = new DatagramPacket(new byte[Constants.FEEDBACK_PKTSIZE] , Constants.FEEDBACK_PKTSIZE);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void setForwardPort(int localPort){
		this.forwardPort = localPort;
	}

	@Override
	public void run() {
		super.run();

		initSocket();

		FIRST_PACKET = true;
		packetBuffer = new ArrayList<Packet>();
		checkList = new boolean[Constants.CWLEN];
		bb =  ByteBuffer.allocate(Constants.FEEDBACK_PKTSIZE);

		while(RUNNING){
			try {
				socket.receive(networkPacket);
				if(firstPacket()){
					initFeedbackSocket(networkPacket);
					socket.setSoTimeout(SO_TIMEOUT);
				}

				handleNetworkPacket(networkPacket);


			} catch (Exception e) {
				Log.i(logWriter, "run()","timeout"); 
				System.err.print("\n["+System.currentTimeMillis()+"]");
				e.printStackTrace();
			}
		}

		cleanUp();

	}


	private boolean firstPacket() {
		if(FIRST_PACKET ){
			FIRST_PACKET = false;
			return true;
		}
		return FIRST_PACKET;
	}

	public void stopRunning(){
		RUNNING = false;
	}
	private void handleNetworkPacket(DatagramPacket networkPacket2) throws IOException {

		Packet packet = Packet.parseNetworkPacket(networkPacket2);
		long now = System.currentTimeMillis();

		if(isNewCodeWord(packet.codeWordNumber) && !packetBuffer.isEmpty()){

			int FEC = packetBuffer.get(0).FEC;
			int DATA = Constants.CWLEN - FEC;

			// send feedback
			sendFeedBack(now, networkPacket2);

			// decode
			byte [] decodedArray = decodeBuffer(DATA,packetBuffer);

			// rebuild images
			rebuildImages( decodedArray, DATA, now, packet);

			
			// finally...
			firstSequenceNumberReceived = packet.sequenceNumber;
			firstSequenceNumberReceivedTime = now;
			packetBuffer.clear();
			checkList = new boolean[Constants.CWLEN];

		}
		else
			lastSequenceNumberReceivedTime = now;
		
		lastSequenceNumberReceived = packet.sequenceNumber;
		packetBuffer.add(packet);
		checkList[lastSequenceNumberReceived]=true;

	}


	private void rebuildImages(byte[] decodedArray, int DATA, long now, Packet packet) {
		ByteBuffer bb;
		packetBuffer.clear();
		Packet tmp;

		//			byte[] intBytes = new byte[Integer.SIZE/8 * 3];
		bb = ByteBuffer.wrap(decodedArray);

		for(int i=0;i<DATA;i++){
			tmp = new Packet();
			tmp.data = new byte[Packet.NET_PAYLOAD];

			System.arraycopy(decodedArray, Packet.PKTSIZE * i + Packet.IMG_METADATA_SIZE, tmp.data, 0,Packet.NET_PAYLOAD);

			tmp.contentId = bb.getInt(Packet.PKTSIZE * i);
			tmp.contentSize = bb.getInt(Packet.PKTSIZE * i + Integer.SIZE/8);
			tmp.contentOffset = bb.getInt(Packet.PKTSIZE * i + Integer.SIZE/8 * 2);
//			System.out.println("receiverthread.rebuildimages() "+i+" recovered cid "+tmp.contentId+", size "+tmp.contentSize+", offset "+tmp.contentOffset);
			packetBuffer.add(tmp);
		}

		// handle images
		TaggedImage tmpti = new TaggedImage(packetBuffer.get(0).contentSize);
		//		System.out.println("ReceiverThread.handleNetworkPacket() first content = "+ packetBuffer.get(0).contentId);
		tmpti.id = packetBuffer.get(0).contentId;
		for(int i=0;i<DATA;i++){
			tmp = packetBuffer.get(i);
			if(tmp.contentId!=-1 && tmp.contentSize>0 ){
				try{
					if(tmp.contentId == tmpti.id){
						//						System.out
						//						.println("contentid: "+tmp.contentId+" contentsize: "+tmp.contentSize+", offset "+ tmp.contentOffset);
						//				System.out
						//						.println("handlenetpacket. Copio "+ Math.min(tmp.contentSize - tmp.contentOffset, Math.min(Packet.NET_PAYLOAD,tmp.contentSize)) +" da tmp che è di "+tmp.data.length+" in tmpti.bytes che è di "+tmpti.bytes.length+" a partire da "+ tmp.contentOffset);

						System.arraycopy(tmp.data, 0, tmpti.bytes, tmp.contentOffset,Math.min(tmp.contentSize - tmp.contentOffset, Packet.NET_PAYLOAD) );

						//							System.out
						//									.println("ReceiverThread.handleNetworkPacket() i="+i+", condition = "+(tmp.contentSize - tmp.contentOffset <= Packet.NET_PAYLOAD ));
						if(tmp.contentSize - tmp.contentOffset <= Packet.NET_PAYLOAD ){
							//							Log.i(logWriter, "receiverthread","putting "+tmpti.id);
							if(imageBuffer.has(1))
								imageBuffer.put(tmpti);
							else
								Log.i(logWriter,"ReceiverThread.handleNetworkPacket()","DROPP!");
						}

					}
					else{
						tmpti = new TaggedImage(tmp.contentSize);
						tmpti.id = tmp.contentId;
						//							System.out
						//									.println("ReceiverThread.handleNetworkPacket() got "+tmpti.id);
						//						System.out
						//								.println("contentid: "+tmp.contentId+" contentsize: "+tmp.contentSize+", offset "+ tmp.contentOffset);
						//						System.out
						//								.println("handlenetpacket. Copio "+ Math.min(tmp.contentSize - tmp.contentOffset, Math.min(Packet.NET_PAYLOAD,tmp.contentSize)) +" da tmp che è di "+tmp.data.length+" in tmpti.bytes che è di "+tmpti.bytes.length+" a partire da "+ tmp.contentOffset);
						System.arraycopy(tmp.data, 0, tmpti.bytes, tmp.contentOffset,Math.min(tmp.contentSize - tmp.contentOffset, Math.min(Packet.NET_PAYLOAD,tmp.contentSize)) );
					}
				}
				catch (Exception e){
					Log.i(logWriter,"receverthread","error in reconstructed metadata - contentid "+tmp.contentId+" i="+i+", size "+tmp.contentSize+", offset "+tmp.contentOffset+", decoded "+decoder.isDecoded());
				}
			}
			else{
				//padding
			}

		}

	}

	private byte[] decodeBuffer(int DATA, ArrayList<Packet> packetBuffer2) {

		// reinit decoder

		decoder = new RQDecoder();
		decoder.init(FECParameters.newParameters(DATA * Packet.PKTSIZE, Packet.PKTSIZE, 1));

		int j;
		int res = 3;

		for( j=0;j<packetBuffer.size()  ;j++){
			res = decoder.handlePacket(packetBuffer2.get(j).data);
			if (res == RQDecoder.DATA_DECODE_COMPLETE)
				break;
		}
		j++;
//		System.out.println("ReceiverThread.handleNetworkPacket() handled "+j+", "+decoder.isDecoded());

		return decoder.getDataAsArray();
	}

	private void sendFeedBack(long now, DatagramPacket networkPacket2) {
		int sequenceNumberWindow = lastSequenceNumberReceived - firstSequenceNumberReceived;
		long time = lastSequenceNumberReceivedTime - firstSequenceNumberReceivedTime;
		long interCodeWordTime = now - lastCodeWordTime;
		lastCodeWordTime = now;
		int thisCodeWordNumber = packetBuffer.get(0).codeWordNumber;
		sendReport(thisCodeWordNumber, estimateR(sequenceNumberWindow, time), countCheckList(checkList) ,interCodeWordTime,networkPacket2);
			
	}

	private void sendReport(int thisCodeWordNumber, double rEst,
			int countCheckList, long interCodeWordTime, DatagramPacket networkPacket2) {

//		Log.i(logWriter, "sendReport()","Entrato in sendreport() ");

		bb.clear();
		bb.putInt(thisCodeWordNumber);
		bb.putDouble(rEst);
		bb.putInt(countCheckList);
		bb.putLong(interCodeWordTime);
		bb.rewind();
		
		info = String.format(" - R:%3.2f kbps, L:%2.2f %%", rEst/1000d, (double)countCheckList/(double)Constants.CWLEN*100d);
		
		byte[] data = new byte[Constants.FEEDBACK_PKTSIZE];
		bb.get(data);
		reportPacket.setData(data);
		reportPacket.setPort(feedbackPort);
		reportPacket.setAddress(networkPacket2.getAddress());
		try {
			feedbackSocket.send(reportPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String recBool = "";
		for (boolean b : checkList) {
			if(b)
				recBool +="O";
			else
				recBool +="_";
			
		} 
//		Log.i(logWriter,"ReceiverThread.handleNetworkPacket()","Checklist: "+recBool);
//		Log.i(logWriter,"ReceiverThread.handleNetworkPacket()",String.format("\nFirstTS(%d):%d\nLastTS(%d):%d",firstSequenceNumberReceived,firstSequenceNumberReceivedTime,lastSequenceNumberReceived,lastSequenceNumberReceivedTime ));

		Log.i(logWriter,"ReceiverThread.handleNetworkPacket()","Sent report for "+thisCodeWordNumber+", estimated "+rEst);
		
	}

	public String getInfo() {
		return info;
	}

	private int countCheckList(boolean[] checkList2) {
		int lost=0;
		for (boolean b : checkList2) {
			if(!b)
				lost++;
		}
		return lost;
	}

	private boolean isNewCodeWord(int codeWordNumber) {
		if(codeWordNumber >  currentCodeWordNumber){
			currentCodeWordNumber = codeWordNumber;
			return true;
		}
		else{
			if(codeWordNumber<currentCodeWordNumber)
				Log.i(logWriter,"ReceiverThread","error: received old codeword!");
			return false;
		}
	}


	private void cleanUp() {
		socket.close();
		feedbackSocket.close();
	}

	public void setFps(int fps) {

	}

	private double estimateR(int sequenceNumberWindow, long time){
		
		Log.i(logWriter,"ReceiverThread.estimateR()","window:"+sequenceNumberWindow+", time:"+time);
		
		return  (sequenceNumberWindow+1) * (Packet.PKTSIZE+Packet.HEADERSIZE+RQDecoder.HEADERSIZE) * 8d / (time/1000d);
	}

}
