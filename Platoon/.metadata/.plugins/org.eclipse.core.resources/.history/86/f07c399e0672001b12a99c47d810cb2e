package com.platoon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class SingleTruck extends Thread implements TruckProperties,Serializable {
	protected final Role role;	
	private String licensePlate;
	private Mode mode;
	protected Status state;
	private boolean temprature,fuelLevel,seatBelt;
	//Needed only for Leading Truck
	transient private static int num_of_clients =0;
	transient private Truck [] Platoon = new Truck[2];
	transient private Truck platoon_truck = null;
	transient private static ServerSocket serverSocket;
	public SingleTruck(Role role)
	{
		this.role = role;		
		this.mode = mode.AUTONOMOUS;
		this.state = Status.NORMAL;
		this.temprature = false;
		this.fuelLevel = false;
		this.seatBelt = false;
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		 //Initialization of Connections
		final SingleTruck truck;	   
		   if(args[0].equalsIgnoreCase("L")) {
			   truck = new SingleTruck(Role.LEAD);
			   truck.setLicensePlate(args[1]);
			   serverSocket = new ServerSocket(9130,0,InetAddress.getByName(null));
			   Socket s = null;
			   while (num_of_clients<2) {
				try {
					System.out.println("Truck "+truck.getLicensePlate()+ "has started the Platoon Server");
					s = serverSocket.accept();
					num_of_clients++;
					System.out.println("A new Truck has connected");
					ObjectOutputStream outStream = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream inStream = new ObjectInputStream(s.getInputStream());
					System.out.println("Creating Handler thread for the new Truck ...");
					Thread t1 = truck.new SendState(s, outStream);
					t1.start();
					Thread t2 = truck.new receiveMessage(s, inStream);
					t2.start();
				} catch (Exception e) {
					System.out.println("Failed to Create Server Socket");
					e.printStackTrace();
				}
			}
		   }else if(args[0].equalsIgnoreCase("F")) {
			   truck = new SingleTruck(Role.FOLLOW);			   
			   truck.setLicensePlate(args[1]);
			   final Socket s = new Socket(InetAddress.getByName(null), 9130);
			   final ObjectOutputStream outStream = new ObjectOutputStream(s.getOutputStream());
			   final ObjectInputStream inStream = new ObjectInputStream(s.getInputStream());			   
			   Thread sendmessage = new Thread(new Runnable() {
				   
				public void run() {
					System.out.println("Follower Truck created sendmessage Thread successfully");
					while(true) {
						try {
							synchronized (truck) {
								System.out.println("Current Socket Status in SendMessage => Closed: " +s.isClosed() +" | Connected: "+s.isConnected() + " | Bound: "+s.isBound());
								System.out.println("Follower Truck is sending its data to Server");
								synchronized(outStream) {
									outStream.writeObject(truck);
									outStream.flush();
									System.out.println("Truck data sent to Sever");
								}
								truck.notifyAll();
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
				   				   
			   });
			   Thread receiveState = new Thread(new Runnable() {
				   
					public void run() {
						System.out.println("Follower Truck created receiveState Thread successfully");
						while (true) {
							try {					
									
									synchronized (truck) {
										System.out.println("Current Socket Status in receiveState => Closed: " +s.isClosed() +" | Connected: "+s.isConnected() + " | Bound: "+s.isBound());
										System.out.println("Before Receiving Status");
										synchronized(inStream) {
											truck.setStatus((Status) inStream.readObject());
										}										
										System.out.println("New Status received from Server");
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
				   });
			   sendmessage.start();
			   receiveState.start();
		   }
		   
	}
	
	class SendState extends Thread{
		
		final ObjectOutputStream o;
		final Socket s;
		public SendState(Socket s,ObjectOutputStream o) {
			this.s=s;
			this.o=o;			
		}
		public void run() {			
				 System.out.println("Before Writing State");
				 System.out.println("Current Socket Status in SendState => Closed: " +s.isClosed() +" | Connected: "+s.isConnected() + " | Bound: "+s.isBound());
				 try {
					 synchronized (state) {						 
						 o.writeObject(state);
					 }
				} catch (IOException e) {
					e.printStackTrace();
				}
				 System.out.println("Successfully wrote State");
				 //outStream.flush();				 					 
				 try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			 
		}
	}
	class receiveMessage extends Thread{
		Truck truck;
		final ObjectInputStream i;
		final Socket s;
		public receiveMessage(Socket s,ObjectInputStream i) {
			truck = null;
			this.s=s;
			this.i=i;			
		}
		public void Run() {			
			while (true) {				
			try {
			//inStream = new ObjectInputStream(client.getInputStream());
				synchronized (truck) {
					System.out.println("Before Receiving Truck");
					System.out.println("Current Socket Status in receiveMessage => Closed: " +s.isClosed() +" | Connected: "+s.isConnected() + " | Bound: "+s.isBound());
					synchronized(s) {
						truck = (Truck) i.readObject();
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
	public String getLicensePlate() {
    	return licensePlate;
    }
    public void setLicensePlate(String lp) {
    	licensePlate = lp;
    }
	public Mode getMode() {
		return mode;
	}


	public void setMode(Mode mode) {
		this.mode = mode;
	}
	
	public Status getStatus() {
		return state;
	}

	public void setStatus(Status state) {
		this.state = state;
	}
	

	public void setState(Status state) {
		this.state = state;
	}

	public boolean isTemprature() {
		return temprature;
	}

	public void setTemprature(boolean temprature) {
		this.temprature = temprature;
	}

	public boolean isFuelLevel() {
		return fuelLevel;
	}

	public void setFuelLevel(boolean fuelLevel) {
		this.fuelLevel = fuelLevel;
	}

	public boolean isSeatBelt() {
		return seatBelt;
	}

	public void setSeatBelt(boolean seatBelt) {
		this.seatBelt = seatBelt;
	}

	public Role getRole() {
		return role;
	}


}

