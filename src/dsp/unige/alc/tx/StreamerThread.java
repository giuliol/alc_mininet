package dsp.unige.alc.tx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

import net.fec.openrq.parameters.FECParameters;
import dsp.unige.alc.rx.TaggedImage;
import dsp.unige.alc.utils.CodeWord;
import dsp.unige.alc.utils.Constants;
import dsp.unige.alc.utils.Log;
import dsp.unige.alc.utils.Packet;
import dsp.unige.alc.utils.RQDecoder;

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

		while(RUNNING || cwbHandle.hasAvailable()){
			if(cwbHandle.hasAvailable()){
				try {
					CodeWord toSend = cwbHandle.get();

//					testCodeword(toSend);
					
					send(toSend);
					Log.i(logWriter, "StreamerThread","Sent codeword "+toSend.codeWordNumber);
				} catch (IOException e ) {
					e.printStackTrace();
					Log.i(logWriter,"StreamerThread","error sending packet");
				}
			}
			else{
				try {
					Thread.sleep(Constants.UDP_SLEEPTIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Log.i(logWriter,"StreamerThread","done, terminating");
		sendTerminationPacket();
		System.out.println("StreamerThread.run() sent, exiting");
		cleanUp();
	}

	private void testCodeword(CodeWord toSend) {
		int DATA;
		ArrayList<Packet> packetBuffer = new ArrayList<Packet>();

		Random r = new Random(System.currentTimeMillis());
		int lost = 0;
		for (Packet packet : toSend.pkts) {
			if(r.nextDouble() > 0.29)
				packetBuffer.add(packet);
			else{
				lost++;
			}
		}

		DATA = Constants.CWLEN - packetBuffer.get(0).FEC;
		RQDecoder decoder = new RQDecoder();
		decoder.init(FECParameters.newParameters(DATA * Packet.PKTSIZE, Packet.PKTSIZE, 1));

		int j;
		int res = 3;

		for( j=0;j<packetBuffer.size()  ;j++){
			res = decoder.handlePacket(packetBuffer.get(j).data);
			if (res == RQDecoder.DATA_DECODE_COMPLETE)
				break;
		}
		j++;

		System.out.println("StreamerThread.testCodeword() FEC="+(packetBuffer.get(0).FEC)+" lost "+lost+", decoded "+decoder.isDecoded());
		ByteBuffer bb;
		packetBuffer.clear();
		Packet tmp;

		byte [] decodedArray = decoder.getDataAsArray();
		//			byte[] intBytes = new byte[Integer.SIZE/8 * 3];
		bb = ByteBuffer.wrap(decodedArray);

		for(int i=0;i<DATA;i++){
			tmp = new Packet();
			tmp.data = new byte[Packet.NET_PAYLOAD];

			System.arraycopy(decodedArray, Packet.PKTSIZE * i + Packet.IMG_METADATA_SIZE, tmp.data, 0,Packet.NET_PAYLOAD);

			tmp.contentId = bb.getInt(Packet.PKTSIZE * i);
			tmp.contentSize = bb.getInt(Packet.PKTSIZE * i + Integer.SIZE/8);
			tmp.contentOffset = bb.getInt(Packet.PKTSIZE * i + Integer.SIZE/8 * 2);
//			System.out.println("StreamerThread.testCodeword() "+i+" recovered cid "+tmp.contentId+", size "+tmp.contentSize+", offset "+tmp.contentOffset);
			packetBuffer.add(tmp);
		}

		// handle images
		TaggedImage tmpti = new TaggedImage(packetBuffer.get(0).contentSize);
		tmpti.id = packetBuffer.get(0).contentId;
		for(int i=0;i<DATA;i++){
			tmp = packetBuffer.get(i);
			if(tmp.contentId!=-1 && tmp.contentSize>0 ){
				try{
					if(tmp.contentId == tmpti.id)
						System.arraycopy(tmp.data, 0, tmpti.bytes, tmp.contentOffset,Math.min(tmp.contentSize - tmp.contentOffset, Packet.NET_PAYLOAD) );
					else{
						tmpti = new TaggedImage(tmp.contentSize);
						tmpti.id = tmp.contentId;
						System.arraycopy(tmp.data, 0, tmpti.bytes, tmp.contentOffset,Math.min(tmp.contentSize - tmp.contentOffset, Math.min(Packet.NET_PAYLOAD,tmp.contentSize)) );
					}
				}
				catch (Exception e){
					Log.i(logWriter,"VALIDATION","error in reconstructed metadata - contentid "+tmp.contentId+" i="+i+", size "+tmp.contentSize+", offset "+tmp.contentOffset+", decoded "+decoder.isDecoded());
				}
			}
			else{
				//padding
			}
		}

	}

	private boolean sendTerminationPacket() {
		System.out.println("Connecting to " + destination.toString()
				+ " on port " + Constants.TERMINATION_SOCKET_PORT);
		try{
			Socket client = new Socket( destination.getHostName(), Constants.TERMINATION_SOCKET_PORT);
			System.out.println("Just connected to "
					+ client.getRemoteSocketAddress());
			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out =
					new DataOutputStream(outToServer);

			out.writeUTF(Constants.TERMINATION_MSG);
			InputStream inFromServer = client.getInputStream();
			DataInputStream in =
					new DataInputStream(inFromServer);
			String response = in.readUTF();
			if(response.equals(Constants.TERMINATION_ACK)){

				client.close();
				return true;
			}
			client.close();
		}
		catch (Exception e ){
			e.printStackTrace();
		}
		return false;
	}

	private void cleanUp() {
		forwardSocket.close();
		Log.i(logWriter,"StreamerThread.cleanUp()","exiting");
	}

	private void send(CodeWord codeWord) throws IOException {
		forwardPacket.setAddress(destination);
		forwardPacket.setPort(forwardPort);

//		Random r = new Random(System.currentTimeMillis());
		for(Packet p : codeWord.pkts){
			
			forwardPacket.setData(p.buildPacket());
			
//			if(r.nextDouble()>0.3)
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
			//forwardPacket = new DatagramPacket(new byte[Packet.PKTSIZE + Packet.HEADERSIZE], Packet.PKTSIZE + Packet.HEADERSIZE);
			forwardPacket = new DatagramPacket(new byte[Packet.PKTSIZE + Packet.HEADERSIZE + RQDecoder.HEADERSIZE], Packet.PKTSIZE + Packet.HEADERSIZE + RQDecoder.HEADERSIZE);
			// new DatagramPacket(new byte[Packet.PKTSIZE + Packet.HEADERSIZE + RQDecoder.HEADERSIZE], Packet.PKTSIZE + Packet.HEADERSIZE + RQDecoder.HEADERSIZE);
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
