package com.platoon;
import java.io.*;
import java.net.*;

public class PlatoonHandler extends Thread{
	//private Truck [] Platoon = new Truck[3];
	private Socket clientSocket;
	private ServerSocket serverSocket = null;	
	private final Truck truck;
	private ObjectOutputStream outStream = null;
	private ObjectInputStream inStream = null;
	
	public PlatoonHandler(Truck truck) {
		this.truck = truck;
	}
	public void createSocket() {
		
		//If the truck is a Leader, initiate connection as a Server
		if (truck.role == Role.LEAD) {
			clientSocket = null;
			try {
				serverSocket = new ServerSocket(TruckProperties.platPort);
				System.out.println("Truck "+TruckProperties.licensePlate+ "has started the Platoon Server");
				clientSocket = serverSocket.accept();
				System.out.println("Truck at "+clientSocket.getInetAddress().getHostAddress()+ "has successfully connected to Platoon!");
				//Platoon[0]=this.truck;
			}catch(IOException e){
				System.out.println("Server failed to initiate: "+e.toString());
				createSocket();
			}
		}
		//Follower truck creates a client socket
		else if(truck.role == Role.FOLLOW) {
			try {
				clientSocket = new Socket(InetAddress.getByName(TruckProperties.platoonIP), TruckProperties.platPort);
				System.out.println("Truck "+TruckProperties.licensePlate+ "has successfully connected to Platoon");
				}catch(IOException e) {
				System.out.println("Client failed to initiate: "+e.toString());
				createSocket();
			}
		}
	}
	//Sending a signal to connected Client. Typically from Leading truck to a follower.
	public void sendState(Socket client, State state) {
		try {
			outStream = new ObjectOutputStream(client.getOutputStream());			
			outStream.writeObject(state);
			outStream.flush();
			outStream.close();
			client.close();
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	//Receiving a signal from leading truck.
	public State receiveState(Socket client) {
		State signal = null;
		try {
			inStream = new ObjectInputStream(client.getInputStream());			
			signal = (State) inStream.readObject();
			inStream.close();
			client.close();
		} catch (Exception e) {			
			e.printStackTrace();
		}		
		return signal;		
	}
	//Sending all the truck data to Server
	public void sendMessage(Socket client) {
		try {
			outStream = new ObjectOutputStream(client.getOutputStream());			
			outStream.writeObject(this.truck);
			outStream.flush();
			outStream.close();
			client.close();
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	//Receive Truck Data from Client
	public Truck receiveMessage(Socket client) {
		Truck truck = null;
		try {
			inStream = new ObjectInputStream(client.getInputStream());			
			truck = (Truck) inStream.readObject();
			inStream.close();
			client.close();
		} catch (Exception e) {			
			e.printStackTrace();
		}		
		return truck;		
	}
	//Closing Connection and Releasing Resources
	public void closeConnection() {
		System.out.println("Closing the connection of "+TruckProperties.licensePlate);
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
	
	
}
