package com.platoon;
import java.io.*;
import java.net.*;

public class PlatoonHandler extends Thread{
	private int num_of_clients =0;
	private Truck [] Platoon = new Truck[2];
	private Truck platoon_truck = null;
	private Socket clientSocket;
	private ServerSocket serverSocket = null;	
	private final Truck truck;
	private ObjectOutputStream outStream = null;
	private ObjectInputStream inStream = null;
	private Status state;
	public PlatoonHandler(Truck truck) {
		this.truck = truck;
	}
	public void createSocket() {
		SendState t_send;
		receiveMessage t_receive;
		//If the truck is a Leader, initiate connection as a Server
		if (truck.role == Role.LEAD) {
			clientSocket = null;			
			try {
				serverSocket = new ServerSocket(TruckProperties.platPort);
				System.out.println("Truck "+truck.getLicensePlate()+ "has started the Platoon Server");
				while(Platoon[1] == null) {					
					clientSocket = serverSocket.accept();
					t_send = new SendState(clientSocket);
					t_send.start();
					t_receive = new receiveMessage(clientSocket);
					t_receive.start();
					System.out.println("Truck :  "+ (num_of_clients++) + " has successfully connected to Platoon !");
					Platoon[num_of_clients] = platoon_truck;
					System.out.println("Truck: " + platoon_truck.getLicensePlate() + "was added to Platoon!");
					num_of_clients++;
				}					
			}catch(IOException e){
				System.out.println("Server failed to initiate: "+e.toString());
				createSocket();
			}
		}
		//Follower truck creates a client socket
		else if(truck.role == Role.FOLLOW) {
			try {
				clientSocket = new Socket(InetAddress.getByName(TruckProperties.platoonIP), TruckProperties.platPort);
				System.out.println("Truck "+truck.getLicensePlate()+ "has successfully connected to Server !");
				//Sending First Message
				SendMessage sendmessage = new SendMessage();
				sendmessage.start();
				receiveState r_state = new receiveState();
				r_state.start();
			}catch(IOException e) {
				System.out.println("Client failed to initiate: "+e.toString());
				createSocket();
			}
		}
	}

	
	//Sending a signal to connected Client. Typically from Leading truck to a follower.
	public class SendState extends Thread{		
		Socket client;
		public SendState(Socket client) {			
			this.client = client;
		}
		public void run() {
			while (true) {
				try {
					outStream = new ObjectOutputStream(client.getOutputStream());
					//Safe Access Needed
					outStream.writeObject(state);
					outStream.flush();
					//outStream.close();
					//client.close();
				} catch (Exception e) {
					System.out.println("Failed to send the Status: " + state);
					e.printStackTrace();
				} 
			} 
		}
		
	}
	
	// Receiving a signal from leading truck.
	// To execute 
	public class receiveState extends Thread {
		
		public void Run(){
			if (truck.role == Role.FOLLOW) {
				while (true) {
					try {
						inStream = new ObjectInputStream(clientSocket.getInputStream());
						//We will need safe access using Locks					
						truck.setStatus((Status) inStream.readObject());
						//inStream.close();
						sleep(10);
						// client.close();
					} catch (Exception e) {
						System.out.println("Failed to receive the State");
						e.printStackTrace();
					} 
				}
			}
			
		}

	}
	
	//Sending all the truck data to Server
	public class SendMessage extends Thread{
			
		
		public void Run() {
			if (truck.role == Role.FOLLOW) {
				while (true) {
				try {
					//We need to do it safe as well
					outStream = new ObjectOutputStream(clientSocket.getOutputStream());
					outStream.writeObject(truck);
					outStream.flush();
					//outStream.close();
					sleep(10);
					//client.close();
				} catch (Exception e) {
					System.out.println("Failed to send the Truck: " + truck.getLicensePlate() + "data");
					e.printStackTrace();
				} 
				} 
			}
			
		}
		}
		
	
	//Receive Truck Data from Client
	public class receiveMessage extends Thread{		
		Truck truck;
		Socket client;
		public receiveMessage(Socket client) {			
			truck = null;
			this.client = client;
			}
		public void Run() {
			while (true) {
				try {
					inStream = new ObjectInputStream(client.getInputStream());			
					truck = (Truck) inStream.readObject();				
					//inStream.close();						
				    //client.close();
				} catch (Exception e) {	
					System.out.println("Failed to receive the Truck Status");
					e.printStackTrace();
				}
			}		
					
		}
		}
		
	
	//Closing Connection and Releasing Resources
	public void closeConnection() {
		System.out.println("Closing the connection of "+truck.getLicensePlate());
		try {
			serverSocket.close();
			clientSocket.close();
			inStream.close();
			outStream.close();
		} catch (IOException e) {
			System.out.println("Closing Connection Failed !");
			e.printStackTrace();
		}
	}
	public Socket getClientSocket() {
		return this.clientSocket;
	}
	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	
}
