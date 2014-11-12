package dsp.unige.alc.rx;

public class TaggedImage {

	public int id;
	public byte[] bytes;
	
	public TaggedImage(int size){
		bytes = new byte[size];
	}
	
	
	@Override
	protected TaggedImage clone() {
		TaggedImage out = new TaggedImage(bytes.length);
		out.id = this.id;
		out.bytes = this.bytes.clone();
		return out;
	}
}
