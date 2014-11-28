package dsp.unige.artifacts;



public class SelfTest {
	
	public static void main(String[] args) {
	
		
		final String [] rArgs = {"0"};
		final String [] tArgs = {"127.0.0.1","0","1"};
		
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				Receiver.main(rArgs);
			}
		})).start();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				Transmitter.main(tArgs);
			}
		})).start();
		
//		RQTest rt = new RQTest();
//		double res = 0;
//		for(int i=0;i< 1000;i++){
//			res = rt.go4(13, 0.3);
//			if(res != 0)
//				System.out.println("Test.enclosing_method() errore residuo "+res);
//		}
//		System.out.println("Test.main() fatto");
			
	}
}
