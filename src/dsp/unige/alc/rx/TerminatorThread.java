package dsp.unige.alc.rx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import dsp.unige.alc.common.Constants;

public class TerminatorThread extends Thread {

	RxMain callerHandle;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			

			ServerSocket serverSocket = new ServerSocket(Constants.TERMINATION_SOCKET_PORT);
//			 System.out.println("Waiting for client on port " +
//			            serverSocket.getLocalPort() + "...");
			Socket server = serverSocket.accept();
//			System.out.println("TerminatorThread.run() Connected");
			DataInputStream in = new DataInputStream(server.getInputStream());
			while(true){
				String msg = in.readUTF();
				if(msg.equals(Constants.TERMINATION_MSG)){
					System.err.write("terminating".getBytes());
					callerHandle.stopRunning();
					DataOutputStream out = new DataOutputStream(server.getOutputStream());
					out.writeUTF(Constants.TERMINATION_ACK);
					server.close();
					serverSocket.close();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setCallerHandle(RxMain rxMain) {
		callerHandle = rxMain;
	}
}
