package connection;

import java.io.*;
import java.net.*;

public class ClientNode {

	private static String hostName; 
	private static int portNumber;
	
	private static Socket clientSocket;
	
	private static PrintWriter out;
	private static BufferedReader in;
	
	private static boolean connected;

	public static void main(String[] args) {
		
		if (args.length != 2) {
			System.err.println("Usage: java ClientNode <remote host> <remote port>");
			System.exit(1);
		}
		
		hostName = args[0];
		portNumber = Integer.parseInt(args[1]);
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				System.in));
		
		connected = false;
		new Thread() {
			@Override
			public void run() {
				boolean polling = true;
				while (polling) {
					try {
						clientSocket = new Socket(hostName, portNumber);
						out = new PrintWriter(clientSocket.getOutputStream(), true);
						in = new BufferedReader(
								new InputStreamReader(
										clientSocket.getInputStream()));
						polling = false;
					} catch (IOException e) {
						try {
							Thread.sleep(200); // 0.2 seconds
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}
					}
				}
				connected = true;
				System.out.println("connected.");
				String receivedMessage;

				try {
					while ((receivedMessage = in.readLine()) != null) {
						System.out.println("couterpart: " + receivedMessage);
					}
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}.start();
		
		
		String userInput;
		try {
			while ((userInput = stdIn.readLine()) != null) {
				
				if (connected) {
					out.println(userInput);
					System.out.println("me: " + userInput);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
