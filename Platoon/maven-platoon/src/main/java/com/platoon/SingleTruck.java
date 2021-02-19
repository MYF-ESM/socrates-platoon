package com.platoon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SingleTruck extends Thread implements TruckProperties,Serializable {
	transient protected final Role role;	
	private String licensePlate;
	transient private Mode mode;
	protected Status state;
	private boolean temprature,fuelLevel,seatBelt;
	transient private Socket t_socket;
	//Needed only for Leading Truck
	transient private static int num_of_clients =0;
	transient private static Socket [] Platoon = new Socket[2];	
	transient private static ServerSocket serverSocket;
	//For Handling Abrupt Closure of Sockets
	
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
			   System.out.println("Truck "+truck.getLicensePlate()+ " has started the Platoon Server");
			   listenToFollowers(truck);			   
		   }else if(args[0].equalsIgnoreCase("F")) {
			   truck = new SingleTruck(Role.FOLLOW);			   
			   truck.setLicensePlate(args[1]);			   
			   connectToPlatoon(truck);		   
		   }		   
		   //Start your logic from here if you want!
	}
	
	public static void listenToFollowers(final SingleTruck truck) {
		while (num_of_clients<2) {
			try {
				System.out.println("Listening to Connections .....");				
				//truck.Platoon[num_of_clients] = serverSocket.accept();
				Socket s = serverSocket.accept();
				System.out.println("A new Truck has connected");
				//ObjectOutputStream outStream = new ObjectOutputStream(truck.Platoon[num_of_clients].getOutputStream());
				//ObjectInputStream inStream = new ObjectInputStream(truck.Platoon[num_of_clients].getInputStream());	
				ObjectOutputStream outStream = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(s.getInputStream());	
				System.out.println("Creating Handler thread for the new Truck ...");
				//Thread t1 = truck.new SendState(truck.Platoon[num_of_clients], outStream,num_of_clients);
				Thread t1 = truck.new SendState(s, outStream,num_of_clients);
				t1.start();
//				Thread t2 = truck.new receiveMessage(truck.Platoon[num_of_clients], inStream,num_of_clients);
				Thread t2 = truck.new receiveMessage(s,inStream,num_of_clients,truck);
				t2.start();				
				num_of_clients++;
			} catch (Exception e) {
				System.out.println("Failed to Create Server Socket");
				e.printStackTrace();
			}
		}
	}
	
	public static void connectToPlatoon(final SingleTruck truck) throws IOException {
		boolean disconnected = true;
		//In case follower truck starts first, keep polling till server port is listening
		while(disconnected) {
			   try {
				truck.t_socket = new Socket(InetAddress.getByName(null), 9130);
				truck.setMode(Mode.PLATOON);
				disconnected=false;
			} catch (IOException e) {
				System.out.println("MAIN TRUCK NOT REACHABLE, TRYING AGAIN ....");					
				continue;
			}
		   }
			final ObjectOutputStream outStream = new ObjectOutputStream(truck.t_socket.getOutputStream());
		   final ObjectInputStream inStream = new ObjectInputStream(truck.t_socket.getInputStream());			   
		   Thread sendmessage = new Thread(new Runnable() {
			   
			public void run() {
				System.out.println("Follower Truck created sendmessage Thread successfully");
				while(true) {
					try {
						synchronized (truck) {
							System.out.println("Current Socket Status in SendMessage => Closed: " +truck.t_socket.isClosed() +" | Connected: "+truck.t_socket.isConnected() + " | Bound: "+truck.t_socket.isBound());
							System.out.println("Follower Truck is sending its data to Server");
							synchronized(outStream) {
								try{
									outStream.writeObject(truck);
								}catch(SocketException e) {
									//Server Socket connection is Lost .. will be handled from Receive Thread
									System.out.println("Lost Connection with Server ....");
									return;
								}
								outStream.flush();
								System.out.println("Truck data sent to Sever");
							}
							truck.notifyAll();
						}
						Thread.sleep(1000);
						continue;
					}catch (Exception e) {
						System.out.println("Failed to send the Truck: " + truck.getLicensePlate() + "data");
						truck.setMode(Mode.AUTONOMOUS);
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
								System.out.println("Current Socket Status in receiveState => Closed: "
										+ truck.t_socket.isClosed() + " | Connected: " + truck.t_socket.isConnected()
										+ " | Bound: " + truck.t_socket.isBound());								
								synchronized (inStream) {
									truck.setStatus((Status) inStream.readObject());
								}
								System.out.println("New Status received from Server: " + truck.getStatus());
							}
							Thread.sleep(1000);
							continue;
						} catch (SocketException e) {
							//Server Socket is disconnected. Cleaning Things up and attempting to reconnect.
							System.out.println("Lost Connection with Server ....");
							System.out.println("Cleaning Up and Reconnecting ...");							
							do {
								try {
									truck.t_socket.close();
									connectToPlatoon(truck);
									return;
								} catch (IOException e1) {
									e1.printStackTrace();
								} 
							} while (!truck.t_socket.isClosed());
								
							
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
	
	class SendState extends Thread{
		
		final ObjectOutputStream o;
		final Socket s;
		final int ID;
		public SendState(Socket s,ObjectOutputStream o,int ID) {
			this.ID=ID;
			this.s=s;
			this.o=o;			
		}
		public void run() {
				int c_status =0;
				 System.out.println("Before Writing State");
				 System.out.println("Current Socket Status in SendState => Closed: " +s.isClosed() +" | Connected: "+s.isConnected() + " | Bound: "+s.isBound());
				 while (true) {
					 c_status++;
					 state = state.NORMAL;
					try {
						synchronized (state) {
							synchronized (o) {
								if (c_status % 2 == 0)
									state = state.BRAKE;
								o.writeObject(state);
								System.out.println("Successfully wrote State "+state.toString());
							}
						}
					}catch(SocketException e) {
						return;
					}
					catch (IOException e) {						
						e.printStackTrace();
						
					}
					//outStream.flush();				 					 
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
				}
				
			 
		}
	}
	class receiveMessage extends Thread{	
		final int ID;
		final ObjectInputStream i;
		final Socket s;
		final SingleTruck server;
		public receiveMessage(Socket s,ObjectInputStream i,int ID,SingleTruck truck) {
			this.ID=ID;
			this.s=s;
			this.i=i;
			server=truck;
		}
		public void run() {			
			while (true) {
				try {
					System.out.println("Current Socket Status in receiveMessage => Closed: " + s.isClosed()
							+ " | Connected: " + s.isConnected() + " | Bound: " + s.isBound());
					synchronized (i) {
						SingleTruck truck = (SingleTruck) i.readObject();
						System.out.println(truck.getLicensePlate());
					}
					System.out.println("After Receiving Truck");
					
					Thread.sleep(1000);
					continue;
					// inStream.close();
					// client.close();
				} catch (SocketException e) {
					num_of_clients--;
					do {
						try {
							s.close();
							listenToFollowers(server);
							return;
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
					} while (!s.isClosed());
				} catch (Exception e) {
					System.out.println("Failed to receive the Truck Status");
					e.printStackTrace();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
	//Getters and Setters
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

