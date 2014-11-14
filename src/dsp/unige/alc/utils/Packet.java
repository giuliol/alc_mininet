package dsp.unige.alc.utils;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;


public class Packet {

	public static final int PKTSIZE = Constants.PKTSIZE;
	public static final int HEADERSIZE = 6 * Integer.SIZE/8;

	public byte[] data;
	public int sequenceNumber;
	public int codeWordNumber;
	public int FEC;
	public int contentId;
	public int contentOffset;
	public int contentSize;

	public Packet(){
		data = new byte[PKTSIZE];
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
		this.data = new byte[PKTSIZE];
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
		int required = (int) Math.ceil((double)img.length / PKTSIZE) ;
		Packet [] out = new Packet[required];
		int bytesLeft;
		for(int i=0;i<required;i++){
			out[i] = new Packet();
			bytesLeft = img.length - PKTSIZE*i;
			System.arraycopy(img, i*PKTSIZE, out[i].data, 0, Math.min(PKTSIZE,bytesLeft));
			out[i].contentId = contentId;
			out[i].contentSize = img.length;
			out[i].contentOffset = i*PKTSIZE;
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

	private byte[] toBytes(int i)
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

		//		System.out.println("Packet.parseNetworkPacket() packet.getdata().length="+packet.getData().length);

		System.arraycopy(networkPacketPayload, 0, header, 0, Packet.HEADERSIZE);
		System.arraycopy(networkPacketPayload, Packet.HEADERSIZE, data, 0,Packet.PKTSIZE + RQDecoder.HEADERSIZE);


		Packet out = new Packet();
		ByteBuffer bb = ByteBuffer.wrap(header);

		// actual packet parsing
		out.codeWordNumber = bb.getInt();
		out.sequenceNumber = bb.getInt();
		out.FEC = bb.getInt();
		out.contentId = bb.getInt();
		out.contentSize = bb.getInt();
		out.contentOffset = bb.getInt();
		out.data = data;

//		System.out.println("Packet.parseNetworkPacket() pacchetto "+out.codeWordNumber+"|"+out.sequenceNumber);
//		int i=0;
//		for (byte b : networkPacketPayload) {
//			if(i%16 == 0)
//				System.out.println("");
//			System.out.print(String.format("%02x ",b));
//
//			i++;
//		}
//		System.out.println("");

//		int howmany = Math.min(out.contentSize - out.contentOffset,Packet.PKTSIZE + RQDecoder.HEADERSIZE) ;
//		System.out.println("Packet.parseNetworkPacket() contains "+howmany+" frame "+out.contentId+"data last bytes ("+(howmany-8)+"):: ");
//		for(int i=8;i>0;i--){
//			System.out.print(String.format("%x ",networkPacketPayload[networkPacketPayload.length-i]));
//		}
//		System.out.print("\n");

		return out;
	}
}
