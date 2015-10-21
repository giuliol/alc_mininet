package dsp.unige.alc.common;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;


public class Packet {

	public static final int PKTSIZE = Constants.PKTSIZE;
	public static final int HEADERSIZE = 6 * Integer.SIZE/8;
	public static final int IMG_METADATA_SIZE = 3 * Integer.SIZE/8;
	public static final int NET_PAYLOAD = PKTSIZE - IMG_METADATA_SIZE;

	public byte[] data;
	public int sequenceNumber;
	public int codeWordNumber;
	public int FEC;
	public int contentId;
	public int contentOffset;
	public int contentSize;

	public Packet(){
		data = new byte[PKTSIZE + RQDecoder.HEADERSIZE];
		sequenceNumber = -1;
		codeWordNumber = -1;
		FEC = -1;
		contentId = -1;
		contentOffset = -1;
		contentSize = -1;
	}

	public Packet(Packet packet) {
		this.sequenceNumber = packet.sequenceNumber+1;
		this.codeWordNumber = packet.codeWordNumber;
		this.FEC = packet.FEC;
		this.contentId = -1;
		this.contentOffset = -1;
		this.contentSize = -1;
		this.data = new byte[PKTSIZE + RQDecoder.HEADERSIZE];
	}

	public boolean isValid(){
		return (
				FEC!=-1 &&
				sequenceNumber != -1 &&
				codeWordNumber != -1 &&
				contentId != -1 &&
				contentOffset != -1 &&
				contentSize != -1
				);
	}

	public static Packet[] fromByteArray(byte[] img, int contentId){
		
		int required = (int) Math.ceil((double)img.length / (NET_PAYLOAD)) ;
		Packet [] out = new Packet[required];
		int bytesLeft;
		for(int i=0;i<required;i++){
			out[i] = new Packet();
			bytesLeft = img.length - NET_PAYLOAD*i;
			System.arraycopy(toBytes(contentId), 0, out[i].data,0 , Integer.SIZE/8);
			System.arraycopy(toBytes(img.length), 0, out[i].data,Integer.SIZE/8, Integer.SIZE/8);
			System.arraycopy(toBytes(i*NET_PAYLOAD), 0, out[i].data,Integer.SIZE/8*2, Integer.SIZE/8);
			System.arraycopy(img, i*NET_PAYLOAD, out[i].data, Integer.SIZE/8 * 3, Math.min(NET_PAYLOAD,bytesLeft));


			out[i].contentId = contentId;
			out[i].contentSize = img.length;
			out[i].contentOffset = i*NET_PAYLOAD;
		}
		return out;
	}

	public byte[] buildPacket() {
		byte[] out = new byte[PKTSIZE + HEADERSIZE + RQDecoder.HEADERSIZE];
		byte[] tmp;

		// write header
		tmp = toBytes(codeWordNumber);
		System.arraycopy(tmp, 0, out, 0, 4);

		tmp = toBytes(sequenceNumber);
		System.arraycopy(tmp, 0 , out,  Integer.SIZE/8, 4);

		tmp = toBytes(FEC);
		System.arraycopy(tmp, 0, out, Integer.SIZE/8 *2, 4);

		tmp = toBytes(contentId);
		System.arraycopy(tmp, 0, out, Integer.SIZE/8 *3, 4);

		tmp = toBytes(contentSize);
		System.arraycopy(tmp, 0, out,  Integer.SIZE/8 *4, 4);

		tmp = toBytes(contentOffset);
		System.arraycopy(tmp, 0, out, Integer.SIZE/8 *5, 4);

		// write payload
		System.arraycopy(data, 0, out, Integer.SIZE/8 *6, PKTSIZE + RQDecoder.HEADERSIZE);

		return out;
	}

	static private byte[] toBytes(int i)
	{
		byte[] result = new byte[4];

		result[0] = (byte) (i >> 24);
		result[1] = (byte) (i >> 16);
		result[2] = (byte) (i >> 8);
		result[3] = (byte) (i /*>> 0*/);

		return result;
	}

	public static Packet parseNetworkPacket(DatagramPacket packet) {
		byte[] data = new byte[Packet.PKTSIZE + RQDecoder.HEADERSIZE];
		byte[] header =  new byte[Packet.HEADERSIZE];

		byte[] networkPacketPayload = packet.getData();

		System.arraycopy(networkPacketPayload, 0, header, 0, Packet.HEADERSIZE);
		System.arraycopy(networkPacketPayload, Packet.HEADERSIZE, data, 0,Packet.PKTSIZE + RQDecoder.HEADERSIZE);

		Packet out = new Packet();
		ByteBuffer bb = ByteBuffer.wrap(header);

		out.codeWordNumber = bb.getInt();
		out.sequenceNumber = bb.getInt();
		out.FEC = bb.getInt();
		out.contentId = bb.getInt();
		out.contentSize = bb.getInt();
		out.contentOffset = bb.getInt();
		out.data = data;

		return out;
	}

	public boolean isTerminationCw() {
		return (codeWordNumber == Integer.MIN_VALUE);
	}


	public static byte[] buildTerminator() {
		Packet out =  new Packet();
		out.codeWordNumber = Integer.MIN_VALUE;
		return out.buildPacket();
	}
}
