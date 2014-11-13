package dsp.unige.alc.rx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.fec.openrq.parameters.FECParameters;
import dsp.unige.ALC.utils.Constants;
import dsp.unige.ALC.utils.Log;
import dsp.unige.ALC.utils.Packet;
import dsp.unige.ALC.utils.RQDecoder;

public class ReceiverThread extends Thread {

	private DatagramSocket socket;
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
			System.out.println("-RX- ReceiverThread.initSocket() listening on "+forwardPort);

		} catch (SocketException e) {
			e.printStackTrace();
			Log.i("ReceiverThread", "error opening socket");
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
		
		packetBuffer = new ArrayList<Packet>();
		checkList = new boolean[Constants.CWLEN];

		while(RUNNING){
			try {
				socket.receive(networkPacket);
				handleNetworkPacket(networkPacket);

			} catch (IOException e) {
				e.printStackTrace();
				Log.i("ReceiverThread","error receiving packet");
			}
		}

		cleanUp();

	}

	private void handleNetworkPacket(DatagramPacket networkPacket2) throws IOException {
		
		Packet packet = Packet.parseNetworkPacket(networkPacket2);
		long now = System.currentTimeMillis();
		int res = 3;
		if(isNewCodeWord(packet.codeWordNumber)){
			// decode
			int j;
			for( j=0;j<packetBuffer.size()  ;j++){
				res = decoder.handlePacket(packetBuffer.get(j).data);
				if (res == RQDecoder.DATA_DECODE_COMPLETE)
					break;
			}
			
			byte [] decodedArray = decoder.getDataAsArray();
			int FEC = packetBuffer.get(0).FEC;
			int DATA = Constants.CWLEN - FEC;
			for(int i=0;i<DATA;i++){
				packetBuffer.get(i).data = new byte[Packet.PKTSIZE];
				System.arraycopy(decodedArray, Packet.PKTSIZE * i, packetBuffer.get(i).data, 0,Packet.PKTSIZE);
			}

			// handle images
			Packet tmp;
			TaggedImage tmpti = new TaggedImage(packetBuffer.get(0).contentSize);
			tmpti.id = packetBuffer.get(0).contentId;
			for(int i=0;i<packetBuffer.size();i++){
				tmp = packetBuffer.get(i);
				if(tmp.contentId!=-1){
					if(tmp.contentId == tmpti.id){
						System.arraycopy(tmp.data, 0, tmpti.bytes, tmp.contentOffset,Math.min(tmp.contentSize - tmp.contentOffset, Packet.PKTSIZE) );
					}
					else{
						if(imageBuffer.has(1))
							imageBuffer.put(tmpti);
						tmpti = new TaggedImage(tmp.contentSize);
						tmpti.id = tmp.contentId;
						System.arraycopy(tmp.data, 0, tmpti.bytes, tmp.contentOffset,Math.min(tmp.contentSize - tmp.contentOffset, Packet.PKTSIZE) );
					}
				}
				else{
//					System.out.println("ReceiverThread.handleNetworkPacket() i:"+i+" FEC packet "+tmp.codeWordNumber+"|"+tmp.sequenceNumber);
				}
			}

			// reinit decoder
			decoder = new RQDecoder();
			decoder.init(FECParameters.newParameters(Constants.CWLEN * Packet.PKTSIZE, Packet.PKTSIZE, 1));

			// send feedback
			int sequenceNumberWindow = lastSequenceNumberReceived - firstSequenceNumberReceived;
			long time = lastSequenceNumberReceivedTime - firstSequenceNumberReceivedTime;
			double rEst = sequenceNumberWindow * (Packet.PKTSIZE+Packet.HEADERSIZE) * 8 / (time/1000d);

			ByteBuffer bb = ByteBuffer.allocate(Constants.FEEDBACK_PKTSIZE);
			bb.putInt(packetBuffer.get(0).codeWordNumber);
			bb.putDouble(rEst);
			bb.putInt(countCheckList(checkList));
			bb.rewind();

			byte[] report =  new byte[Constants.FEEDBACK_PKTSIZE];
			bb.get(report);
			
			DatagramPacket reportPacket = new DatagramPacket(report, report.length, networkPacket2.getAddress(), backwardPort);
			socket.send(reportPacket);

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
				Log.i("ReceiverThread","error: received old codeword!");
			return false;
		}
	}


	private void cleanUp() {
		socket.close();
	}

	public void setFps(int fps) {

	}


}
