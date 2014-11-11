package dsp.unige.ALC.TX;

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
	
	public boolean has(int bytes){
		return (bytes <= data.length - position);
	}
	
	public void put(Packet[] in){
		for(int i=0;i<in.length;i++)
			data[position++] = in[i];
	}
	
	public Packet[] getDataClone(){
		return data.clone();
	}
	
	public byte[] getData(){
		byte [] out =  new byte[data.length * Packet.PKTSIZE];
		for(int i=0;i<data.length;i++){
			System.arraycopy(data[i].data, 0, out, i*Packet.PKTSIZE, Packet.PKTSIZE);
		}
		return out;
	}
}
