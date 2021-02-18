package com.platoon;
import java.io.*;
import java.net.*;

import javax.sql.rowset.spi.SyncResolver;

public class PlatoonHandler extends Thread implements Serializable{
	transient private int num_of_clients =0;
	transient private Truck [] Platoon = new Truck[2];
	transient private Truck platoon_truck = null;
	//private Socket clientSocket;
	transient private ServerSocket serverSocket = null;	
	transient private final Truck truck;
	//private ObjectOutputStream outStream = null;
	//private ObjectInputStream inStream = null;
	private Status state = Status.NORMAL;
	public PlatoonHandler(Truck truck) {
		this.truck = truck;
	}
	public void createSocket() {
		//If the truck is a Leader, initiate connection as a Server
		if (truck.role == Role.LEAD) {						
			try {
				serverSocket = new ServerSocket(9130,0,InetAddress.getByName(null));
				System.out.println("Truck "+truck.getLicensePlate()+ "has started the Platoon Server");
				//while(num_of_clients < 2) {					
					Socket clientSocket = serverSocket.accept();
					clientSocket.setKeepAlive(true);
					System.out.println("Truck :  "+ (num_of_clients++) + " has successfully connected to Platoon !");
					new Thread(new receiveMessage(clientSocket)).start();
					//new Thread(new SendState(clientSocket)).start();										
					//Platoon[num_of_clients] = platoon_truck;
					//System.out.println("Truck: " + platoon_truck.getLicensePlate() + "was added to Platoon!");
					//num_of_clients++;
				//}					
			}catch(IOException e){
				System.out.println("Server failed to initiate: "+e.toString());
				createSocket();
			}
		}
		//Follower truck creates a client socket
		else if(truck.role == Role.FOLLOW) {
			try {
				Socket clientSocket = new Socket(InetAddress.getByName(null), 9130);
				clientSocket.setKeepAlive(true);
				System.out.println("Truck "+truck.getLicensePlate()+ " has successfully connected to Server !");
				//Sending First Message				
				SendMessage sendmessage = new SendMessage(clientSocket);
				sendmessage.start();
				//receiveState r_state = new receiveState(clientSocket);
				//r_state.start();				
				
			}catch(Exception e) {
				System.out.println("Client failed to initiate: "+e.toString());
				createSocket();
			}
		}
	}

	
	//Sending a signal to connected Client. Typically from Leading truck to a follower.
	public class SendState extends Thread{		
		Socket client;
		ObjectOutputStream outStream;
		public SendState(Socket client) throws IOException {			
			this.client = client;
			outStream = new ObjectOutputStream(client.getOutputStream());
			System.out.println("Created Output Stream in SendState thread");
		}
		public void run() {
			System.out.println("Entered Run() inside SendState Thread");			
			while (true) {									
			try {		
					 synchronized (state) {
						 System.out.println("Before Writing State");
						 System.out.println("Current Socket Status in SendState => Closed: " +client.isClosed() +" | Connected: "+client.isConnected() + " | Bound: "+client.isBound());
						 synchronized(client) {							 
							 outStream.writeObject(state);
							 System.out.println("Successfully wrote State");
							 outStream.flush();
						 }					 
						 Thread.sleep(1000);
						 continue;
					 }					 
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
		Socket client;
		ObjectInputStream inStream;
		public receiveState(Socket client) throws IOException {
			this.client = client;
			inStream = new ObjectInputStream(client.getInputStream());
			System.out.println("Created Input Stream in receiveState Thread");
		}

		public void Run() {
			
			if (truck.role == Role.FOLLOW) {
				while (true) {
				try {					
						
						synchronized (truck) {
							System.out.println("Before Reading Status");
							System.out.println("Current Socket Status in receiveState => Closed: " +client.isClosed() +" | Connected: "+client.isConnected() + " | Bound: "+client.isBound());
							synchronized(client) {
								truck.setStatus((Status) inStream.readObject());
							}
							
							System.out.println("After Reading Status");
						}
						// inStream.close();
						// client.close();						
						Thread.sleep(1000);
						continue;
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
		Socket client;
		ObjectOutputStream outStream;
		public SendMessage(Socket client) throws IOException {
			this.client = client;			
			outStream = new ObjectOutputStream(client.getOutputStream());
			System.out.println("Created Output Stream in SendMessage Thread");
		}		
		public void Run() {
			
			if (truck.role == Role.FOLLOW) {
				while (true) {
			try {				
							
					synchronized (truck) {
						System.out.println("Before Sending Truck to Server");
						System.out.println("Current Socket Status in SendMessage => Closed: " +client.isClosed() +" | Connected: "+client.isConnected() + " | Bound: "+client.isBound());
						synchronized(client) {
							outStream.writeObject(truck);
							outStream.flush();
							System.out.println("Truck data sent to Sever");
						}
						
					}
					//outStream.close();			
					//client.close();
					Thread.sleep(1000);
					continue;
				}catch (Exception e) {
					System.out.println("Failed to send the Truck: " + truck.getLicensePlate() + "data");
					e.printStackTrace();
			}
			}
		}
		}
		}
		
	
	/**
	 * Receive Truck Data from Client
	 * Truck is local variable
	 * Logic should be implemented inside the function to update Platoon State
	 * */
	public class receiveMessage extends Thread{		
		Truck truck;
		Socket client;
		ObjectInputStream inStream;	
		public receiveMessage(Socket client) throws IOException {			
			truck = null;
			this.client = client;
			inStream = new ObjectInputStream(client.getInputStream());
			System.out.println("Created Input Stream in receiveMessage Thread");
			}
		public void Run() {			
			while (true) {				
			try {
			//inStream = new ObjectInputStream(client.getInputStream());
				synchronized (truck) {
					System.out.println("Before Receiving Truck");
					System.out.println("Current Socket Status in receiveMessage => Closed: " +client.isClosed() +" | Connected: "+client.isConnected() + " | Bound: "+client.isBound());
					synchronized(client) {
						truck = (Truck) inStream.readObject();
					}
						
					System.out.println("After Receiving Truck");
				}
				Thread.sleep(1000);
				continue;
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
//	public void closeConnection() {
//		System.out.println("Closing the connection of "+truck.getLicensePlate());
//		try {
//			serverSocket.close();
//			clientSocket.close();
//			inStream.close();
//			outStream.close();
//		} catch (IOException e) {
//			System.out.println("Closing Connection Failed !");
//			e.printStackTrace();
//		}
//	}	
		
}
