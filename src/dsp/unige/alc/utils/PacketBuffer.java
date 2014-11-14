package dsp.unige.ALC.utils;


public class PacketBuffer {

	Packet[] data;
	int position;
	
	public PacketBuffer(){
		
	}
	
	public void init(int size){
		data = new Packet[size];
		position = 0;
	}
	
	public void reset(){
		int l = data.length;
		init(l);
	}
	
	public boolean hasBytesAvailable(int bytes){
		return (Math.ceil((double)bytes/Packet.PKTSIZE) <= data.length - position);
	}
	
	public boolean hasPacketsAvailable(int packets){
		return (packets <= data.length - position);
	}
	
	public void put(Packet[] in){
		for(int i=0;i<in.length;i++){
			data[position++] = in[i];
		}
	}
	
	public Packet[] getDataClone(){
		return data.clone();
	}
	
	public byte[] getData(){
		byte [] out =  new byte[data.length * Packet.PKTSIZE];
		for(int i=0;i<data.length;i++){
			if(data[i]==null)
				data[i] = new Packet(data[i-1]);
			System.arraycopy(data[i].data, 0, out, i*Packet.PKTSIZE, Packet.PKTSIZE);
		}
		return out;
	}

	public void status() {
		System.out.println("PacketBuffer.status() position: "+position+", total: "+data.length);
	}

	public void fillWithEncoded(byte[][] packetsBytes) {
//		System.out.println("PacketBuffer.fillWithEncoded() primo byte "+packetsBytes[0][0]);
		for(int i=0;i<data.length;i++){
			data[i].data = packetsBytes[i];
//			for(int b=0;b<data[i].data.length;b++){
//				data[i].data[b] = packetsBytes[i][b];
//			}
		}
//		System.out.println("PacketBuffer.fillWithEncoded() data[0].data.length="+data[0].data.length);

//		System.out.println("PacketBuffer.fillWithEncoded() frame "+data[0].contentId+", "+data[0].data[0]);
	}

	public Packet[] getPackets() {
//		System.out.println("PacketBuffer.getPackets() frame "+data[0].contentId+", "+data[0].data[0]);
		return data;
	}

	public void put(Packet in) {
		data[position++] = in;
	}
}
