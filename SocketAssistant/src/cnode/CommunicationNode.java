package cnode;
import java.io.*;

import connection.*;

public class CommunicationNode implements MessageReceiver {

	private static int localServerPort;
	
	private static ServerNode server;
	
	public static void main(String[] args) {

		if (args.length != 1) {
			System.err.println("Usage: java CommunicationNode <local port>");
			System.exit(1);
		}

		CommunicationNode node = new CommunicationNode();
		localServerPort = Integer.parseInt(args[0]);
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				System.in));

		server = new ServerNode(localServerPort, node);
		server.run();
		

		String userInput;
		try {
			while ((userInput = stdIn.readLine()) != null) {
				
				if (server.isConnected()) {
					server.send(userInput);
					System.out.println("me: " + userInput);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void receive(String message) {
		System.out.println("counterpart: " + message);
	}

	@Override
	public void reestablishConnection() {
		// TODO Auto-generated method stub
		
	}

}
