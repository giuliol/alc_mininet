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
					send(toSend);
					Log.i(logWriter, "StreamerThread","Sent codeword "+toSend.codeWordNumber);
				} catch (IOException e ) {
					e.printStackTrace();
					Log.i(logWriter,"StreamerThread","error sending packet");
				}
			}
			else{
				try {
					Thread.sleep(Constants.INTERCODEWORD_SLEEPTIME);
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
