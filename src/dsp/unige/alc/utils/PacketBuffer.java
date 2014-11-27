package dsp.unige.alc.utils;



public class PacketBuffer {

	Packet[] data;
	int position;
	
	int fec;
	
	public void setFec(int fec) {
		this.fec = fec;
	}
	
	
	public PacketBuffer(){
		
	}
	
	public void init(int size) {
		data = new Packet[size];
		position = 0;
	}
	
	public void reset() throws Exception{
		int l = data.length;
		init(l);
	}
	
	public boolean hasBytesAvailable(int bytes){
		return (Math.ceil((double)bytes/(Packet.NET_PAYLOAD)) <= (data.length - fec) - position);
	}
	
	public boolean hasPacketsAvailable(int packets){
		return (packets <= data.length - position);
	}
	
	public void put(Packet[] in){
		for(int i=0;i<in.length;i++){
			
			data[position++] = in[i];
//			System.out.println("PacketBuffer.put() data["+(position-1)+"].contentId="+data[position-1].contentId+", size: "+data[position-1].contentSize + "offset: "+data[position-1].contentOffset); 
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
		Log.i("PacketBuffer.status()","position: "+position+", total: "+data.length);
	}

	public void fillWithEncoded(byte[][] packetsBytes) {
		for(int i=0;i<data.length;i++){
			data[i].data = packetsBytes[i];
		}
	}

	public Packet[] getPackets() {
		return data;
	}

	public void put(Packet in) {
		data[position++] = in;
	}

	public int occupancy() {
		return position;
	}
}
