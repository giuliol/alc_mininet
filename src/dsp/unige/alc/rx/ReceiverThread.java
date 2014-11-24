package dsp.unige.alc.rx;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.fec.openrq.parameters.FECParameters;
import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Log;
import dsp.unige.alc.utils.Packet;
import dsp.unige.alc.utils.RQDecoder;

public class ReceiverThread extends Thread {

	private static final int SO_TIMEOUT = 2000;
	private DatagramSocket socket;
	private Socket feedbackSocket;
	private DataOutputStream feedbackChannelWriter;
	private DatagramPacket networkPacket;
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
	private int backwardPort;
	private long lastCodeWordTime;
	private Writer logWriter;
	private RxMain callerHandle;
	private boolean FIRST_PACKET;

	public void setCallerHandle(RxMain callerHandle) {
		this.callerHandle = callerHandle;
	}

	public void setLogWriter(Writer logWriter) {
		this.logWriter = logWriter;
	}

	public void setBackwardPort(int listeningPort) {
		this.backwardPort = listeningPort;
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

		while(RUNNING){
			try {
				socket.receive(networkPacket);
				if(firstPacket()){
					initFeedbackSocket(networkPacket);
					socket.setSoTimeout(SO_TIMEOUT);
				}
				handleNetworkPacket(networkPacket);

			} catch (IOException e) {
				Log.i(logWriter,"ReceiverThread","timeout ");
				try {
					if(feedbackSocket.isConnected())
						feedbackChannelWriter.writeBoolean(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		cleanUp();

	}


	private void initFeedbackSocket(DatagramPacket networkPacket2) {
		System.out.println("ReceiverThread.initFeedbackSocket() inizializzo");
		try {
			feedbackSocket = new Socket(networkPacket2.getAddress(),backwardPort);
			feedbackChannelWriter = new DataOutputStream(feedbackSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

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
		callerHandle.stopRunning();
	}
	private void handleNetworkPacket(DatagramPacket networkPacket2) throws IOException {

		Packet packet = Packet.parseNetworkPacket(networkPacket2);
		if(packet.isTerminationCw()){
			Log.i(logWriter,"ReceiverThread.handleNetworkPacket()","termination codeword received");
			stopRunning();
			return;
		}
		long now = System.currentTimeMillis();
		int res = 3;
		if(isNewCodeWord(packet.codeWordNumber)){

			// send feedback
			int sequenceNumberWindow = lastSequenceNumberReceived - firstSequenceNumberReceived;
			long time = lastSequenceNumberReceivedTime - firstSequenceNumberReceivedTime;
			long interCodeWordTime = now - lastCodeWordTime;
			lastCodeWordTime = now;
			int thisCodeWordNumber = packetBuffer.get(0).codeWordNumber;
			sendFeedBack(thisCodeWordNumber, estimateR(sequenceNumberWindow, time), countCheckList(checkList) ,interCodeWordTime,networkPacket2);

			// decode

			///DEBUG
			ArrayList<Integer> lista = new ArrayList<>();
			for(int i=0;i<checkList.length;i++)
			{
				if(!checkList[i])
					lista.add(i);
			}
			String persi = "";
			for (Integer integer : lista) {
				persi += " "+integer;
			}
			////DEBUG

			// reinit decoder
			int FEC = packetBuffer.get(0).FEC;
			int DATA = Constants.CWLEN - FEC;
			decoder = new RQDecoder();
			decoder.init(FECParameters.newParameters(DATA * Packet.PKTSIZE, Packet.PKTSIZE, 1));

			int j;
			for( j=0;j<packetBuffer.size()  ;j++){
				res = decoder.handlePacket(packetBuffer.get(j).data);
				if (res == RQDecoder.DATA_DECODE_COMPLETE)
					break;
			}
			j++;
			System.out.println("ReceiverThread.handleNetworkPacket() handled "+j+", lost "+persi+", "+decoder.isDecoded());

			byte [] decodedArray = decoder.getDataAsArray();

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
				packetBuffer.add(tmp);
			}

			// handle images
			TaggedImage tmpti = new TaggedImage(packetBuffer.get(0).contentSize);
			System.out.println("ReceiverThread.handleNetworkPacket() first content = "+ packetBuffer.get(0).contentId);
			tmpti.id = packetBuffer.get(0).contentId;
			int imageCount = 1;
			for(int i=0;i<DATA;i++){
				tmp = packetBuffer.get(i);
				if(tmp.contentId!=-1 && tmp.contentSize>0 && tmp.contentId >-1 ){
					try{
						if(tmp.contentId == tmpti.id){
							//						System.out
							//						.println("contentid: "+tmp.contentId+" contentsize: "+tmp.contentSize+", offset "+ tmp.contentOffset);
							//				System.out
							//						.println("handlenetpacket. Copio "+ Math.min(tmp.contentSize - tmp.contentOffset, Math.min(Packet.NET_PAYLOAD,tmp.contentSize)) +" da tmp che è di "+tmp.data.length+" in tmpti.bytes che è di "+tmpti.bytes.length+" a partire da "+ tmp.contentOffset);

							System.arraycopy(tmp.data, 0, tmpti.bytes, tmp.contentOffset,Math.min(tmp.contentSize - tmp.contentOffset, Packet.NET_PAYLOAD) );
						
//							System.out
//									.println("ReceiverThread.handleNetworkPacket() i="+i+", condition = "+(tmp.contentSize - tmp.contentOffset <= Packet.NET_PAYLOAD ));
							if(tmp.contentSize - tmp.contentOffset <= Packet.NET_PAYLOAD )
								if(imageBuffer.has(1))
									imageBuffer.put(tmpti);
								else
									Log.i(logWriter,"ReceiverThread.handleNetworkPacket()","DROPP!");
							
						}
						else{
							imageCount++;
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
						Log.i(logWriter,"receverthread","error in reconstructed metadata");
					}
				}
				else{
					//					System.out.println("ReceiverThread.handleNetworkPacket() i:"+i+" FEC packet "+tmp.codeWordNumber+"|"+tmp.sequenceNumber);
				}

			}
			
			imageBuffer.setReceived(imageCount);
			
			firstSequenceNumberReceived = packet.sequenceNumber;
			firstSequenceNumberReceivedTime = now;

			lastSequenceNumberReceived = packet.sequenceNumber;
			packetBuffer.clear();
			checkList = new boolean[Constants.CWLEN];

			packetBuffer.add(packet);
			checkList[packet.sequenceNumber]=true;

		}
		else{
			lastSequenceNumberReceived = packet.sequenceNumber;
			lastSequenceNumberReceivedTime = now;

			packetBuffer.add(packet);
			checkList[lastSequenceNumberReceived]=true;
		}

	}


	private void sendFeedBack(int thisCodeWordNumber, double rEst,
			int countCheckList, long interCodeWordTime, DatagramPacket networkPacket2) {

		try {
			while(!feedbackSocket.isConnected()){

			}
			feedbackChannelWriter.writeBoolean(true);
			feedbackChannelWriter.writeInt(thisCodeWordNumber);
			feedbackChannelWriter.writeDouble(rEst);
			feedbackChannelWriter.writeInt(countCheckList);
			feedbackChannelWriter.writeLong(interCodeWordTime);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.i(logWriter,"ReceiverThread.handleNetworkPacket()","Sent report for "+thisCodeWordNumber+", estimated "+rEst);

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
		try {
			feedbackChannelWriter.close();
			feedbackSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setFps(int fps) {

	}

	private double estimateR(int sequenceNumberWindow, long time){
		return  sequenceNumberWindow * (Packet.PKTSIZE+Packet.HEADERSIZE+RQDecoder.HEADERSIZE) * 8 / (time/1000d);
	}


}
