package com.platoon;

/**
 * Simple implementation of a truck platoon scenario using TCP/IP connection.
 * @author Mohamed Abdulmaksoud, Vasudha Kashyap, Priyadharsini Varadharajan
 * @version 1.0
 */

import java.io.IOException;
import java.util.Scanner;
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
	/**Either AUTONOMOUS OR PLATOON*/
	transient private Mode mode;
	/**Used to detect interrupts from outside,Typically keyboard */
	protected Status interrupt_received;
	/**Current Status of Truck based on its local data computation*/
	protected Status local_state;
	/**General Status of the whole Platoon*/
	protected Status Platoon_Status;
	private boolean temprature,fuelLevel,seatBelt;
	/**Local Socket, used by Follower to connect to Leader*/
	transient private Socket t_socket;
	//Needed only for Leading Truck
	/**A counter for tracking number of connected clients*/
	transient private static int num_of_clients =0;
	/**An array of SingleTruck objects used to track latest updates by connected trucks*/
	private static SingleTruck[] TruckList = new SingleTruck[2];
	/**Used to listening and connecting to other Trucks*/
	transient private static ServerSocket serverSocket;
	/**An obstacle in front of the truck*/
	private static boolean obstacle_detected = false;
	/**A truck is overheating*/
	private static boolean overheat_detected = false;
	/**Fuel Level is Low*/
	private static boolean fuel_level_low = false;
	/**Braking signal is applied*/
	static boolean brake_pressed = false;
	/**
	 * Creates a Truck with default role in the platoon.
	 * @param role The Truck role in the platoon
	 * */	
	public SingleTruck(Role role)
	{
		this.role = role;
		this.mode = mode.AUTONOMOUS;
		this.Platoon_Status = Status.NORMAL;
		this.local_state = Status.NORMAL;
		this.temprature = false;
		this.fuelLevel = false;
		this.seatBelt = false;
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		 //Initialization of Connections
		final SingleTruck truck;
		//Checking whether the started instance is a Leader/Follower Truck
		   if(args[0].equalsIgnoreCase("L")) {
			   //Leader Truck
			   truck = new SingleTruck(Role.LEAD);
			   //Set licensePlate variable to second argument.
			   truck.setLicensePlate(args[1]);
			   //Initializing the server socket to be ready for connections
			   serverSocket = new ServerSocket(9130);
			   System.out.println("Truck "+truck.getLicensePlate()+ " has started the Platoon Server");
			   //Call helper function to listen to incoming connections
			   listenToFollowers(truck);
			   //Call helper function to read keyboard interrupts
			   truck.new readInterrupts().start();
		   }else if(args[0].equalsIgnoreCase("F")) {
			   //Follower Truck
			   truck = new SingleTruck(Role.FOLLOW);
			 //Set licensePlate variable to second argument.
			   truck.setLicensePlate(args[1]);
			   System.out.println("Adding to arryalost");
			 //Call helper function to connect to Leader Truck
			   connectToPlatoon(truck);	
			 //Call helper function to read keyboard interrupts
			   truck.new readInterrupts().start();			   
		   }		   
		   
	}
	/**
	 * Helper function to listen to incoming connections to a truck.
	 * Responsible for creating new communication handler threads for every incoming connection
	 * After successful connections, responsible
	 * @param truck The Leader Truck
	 * */
	public static void listenToFollowers(final SingleTruck truck) {
		//For this case study, only 2 follower trucks are allowed in platoon
		while (num_of_clients<2) {
			try {
				System.out.println("Listening to Connections .....");			
				//Blocking call waiting for new connection
				Socket s = serverSocket.accept();
				System.out.println("A new Truck has connected");					
				ObjectOutputStream outStream = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(s.getInputStream());	
				System.out.println("Creating Handler thread for the new Truck ...");
				Thread t1 = truck.new SendState(s, outStream,num_of_clients);
				t1.start();			
				Thread t2 = truck.new receiveMessage(s,inStream,num_of_clients,truck);
				t2.start();	
				num_of_clients++;				
			} catch (Exception e) {
				System.out.println("Failed to Create Server Socket");
				e.printStackTrace();
			}
		}
		
		
	}
	
	
	/**
	 * Helper function to listen to connect to the leading truck.
	 * Responsible for creating 2 communication handler thread. For sending and receiving messages to/from Leading truck respectively	 * 
	 * @param truck The Follower Truck
	 * */
	public static void connectToPlatoon(final SingleTruck truck) throws IOException {
		//Flag to check connectivity of Truck
		boolean disconnected = true;
		//In case follower truck starts first, keep polling till server port is listening
		while(disconnected) {
			   try {
				truck.t_socket = new Socket(InetAddress.getByName(TruckProperties.platoonIP), 9130);
				//If successful connection, the truck is now in PLATOON mode
				truck.setMode(Mode.PLATOON);
				disconnected=false;
			} catch (IOException e) {
				System.out.println("MAIN TRUCK NOT REACHABLE, TRYING AGAIN ....");
				continue;
			}
		   }
			final ObjectOutputStream outStream = new ObjectOutputStream(truck.t_socket.getOutputStream());
		   final ObjectInputStream inStream = new ObjectInputStream(truck.t_socket.getInputStream());
		   /**
		    * Handler used to send Truck object to the server (Leading Truck)
		    * Does not include the error handling mechanism to reconnect to the leading truck
		    * in case of abrupt termination of connection
		    * Includes the safe termination of thread in case of lost connection
		    * */
		   Thread sendmessage = new Thread(new Runnable() {
			   
			public void run() {
				System.out.println("Follower Truck created sendmessage Thread successfully");
				while(true) {
					try {
						synchronized (truck) {
							synchronized(outStream) {
								try{
									System.out.println("status sent to server" + truck.getlocal_state());
									outStream.reset();
									outStream.writeObject(truck);
								}catch(SocketException e) {
									//Server Socket connection is Lost .. will be handled from Receive Thread
									System.out.println("Lost Connection with Server ....");
									return;
								}
								outStream.flush();
							}
							truck.notifyAll();
						}
						Thread.sleep(1000);
						continue;
					}catch (Exception e) {
						//A connection error with the Server, reset Mode and let Receive Thread handle reconnectivity
						System.out.println("Failed to send the Truck: " + truck.getLicensePlate() + "data");
						truck.setMode(Mode.AUTONOMOUS);
						e.printStackTrace();
				}
				}
			}

		   });
		   /**
		    * Handler used to send Truck object to the server (Leading Truck)
		    * Includes the error handling mechanism to reconnect to the leading truck
		    * in case of abrupt termination of connection
		    * Includes the safe termination of thread in case of lost connection
		    * */
		   Thread receiveState = new Thread(new Runnable() {

			   public void run() {
				   System.out.println("Follower Truck created receiveState Thread successfully");
				   while (true) {
					   try {

						   synchronized (truck) {							
							   synchronized (inStream) {
								   truck.setPlatoonStatus((Status) inStream.readObject());
								   truck.setlocal_state(truck.getPlatoonStatus());					
								}
								System.out.println("New Status received from Server: " + truck.getPlatoonStatus());
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
									truck.setMode(Mode.AUTONOMOUS);
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
		   /** Receive interrupt from keyboard to introduce obstacle or fuel level or over heat conditions in the client trucks
		    * The following characters are used to depict the related status:
		    * 'O' => Obstacle
		    * 'N' => Normal
		    * 'H' => Overheat
		    * 'F' => Low Fuel Level
		    *  */
			   Thread interrupt = new Thread(new Runnable() {				   
				   public void run()
				   {
					   while(true)
					   {
						   Scanner scan = new Scanner(System.in);
						   if(scan.hasNext())
						   {
							   String s1 = scan.nextLine();
							   if(s1.equals("O"))
							   {
								   obstacle_detected = true;
							   }
							   if(s1.equals("N"))
							   {
								   obstacle_detected = false;
								   overheat_detected = false;
								   fuel_level_low = false;
							   }
							   if(s1.equals("H"))
							   {
								   overheat_detected = true;
							   }
							   if(s1.equals("F"))
							   {
								   fuel_level_low = true;
							   }
						   }
					   } 
				   }
				   
			   });
			   /**
			    * Helper thread to simulate the autonomous behavior of the follower truck
			    * */
			   Thread Localstatus = new Thread(new Runnable() {

				   public void run()
				   {
					   while(true)
					   {
						   if(obstacle_detected)
						   {
							   synchronized(truck) {
								   
								   /* Check whether the trucks are in platoon mode or autonomous mode and set the status accordingly*/
								   if(truck.getMode() == Mode.PLATOON)
								   {
									   truck.setInterrupt_received(Status.BRAKE);
								   }
								   else
								   {
									   System.out.println("Obstacle detected in front of Truck"+ truck.getLicensePlate());
									   truck.setlocal_state(Status.BRAKE);
									   System.out.println("Distance to the preceding truck increased due to obstacle");
									   System.out.println("Status of the Truck is: " + truck.getlocal_state());
								   }
							   }
						   }
						   if(overheat_detected)
						   {
							   synchronized(truck) {
								   if(truck.getMode() == Mode.PLATOON)
								   {
									   truck.setInterrupt_received(Status.OVERHEAT);
								   }
								   else
								   {
									   System.out.println("Overheat detected at Truck"+ truck.getLicensePlate());
									   System.out.println("Stop the Truck");
									   truck.setlocal_state(Status.STOP);
									   System.out.println("Status of the Truck is: " + truck.getlocal_state());
								   }

							   }
						   }
						   if(! ((obstacle_detected) || (overheat_detected)))
						   {
							   synchronized(truck) {
								   if(truck.getMode() == Mode.PLATOON)
								   {
									   truck.setInterrupt_received(Status.NORMAL);
								   }
								   else
								   {
									   System.out.println("Truck"+ truck.getLicensePlate()+"is running normally in autonomous mode");
									   truck.setlocal_state(Status.NORMAL);
									   System.out.println("Status of the Truck is: " + truck.getlocal_state());
								   }

							   }
						   }
						   if(fuel_level_low)
						   {
							   synchronized(truck) {
								   if(truck.getMode() == Mode.PLATOON)
								   {
									   truck.setInterrupt_received(Status.FUEL_DEPLETED);
								   }
								   else
								   {
									   System.out.println("Truck"+ truck.getLicensePlate()+"is running out of fuel in autonomous mode");
									   truck.setlocal_state(Status.FUEL_DEPLETED);
									   System.out.println("Status of the Truck is: " + truck.getlocal_state());
									   System.out.println("Search for the nearest gas station");
								   }

							   }
						   }
						   
						   try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					   }
				   }
				   
			   });
		   sendmessage.start();
		   receiveState.start();
		   interrupt.start();
		   Localstatus.start(); 
	}
	/**
	 * Helper thread to read keyboard interrupts for the Platoon leader.
	 * */
	class readInterrupts extends Thread{
		
			public void run() {
						Scanner server_interrupt = new Scanner(System.in);
		while(true)
		{
			
			if(server_interrupt.hasNext())
			{
				String s1 = server_interrupt.nextLine();
			   if(s1.equals("O"))
			   {
				   obstacle_detected = true;
			   }
			   if(s1.equals("N"))
			   {
				   obstacle_detected = false;
				   overheat_detected = false;
				   fuel_level_low = false;
			   }
			   if(s1.equals("H"))
			   {
				   overheat_detected = true;
			   }
			   if(s1.equals("F"))
			   {
				   fuel_level_low = true;
				   System.out.println("Fuel level LOW : locating the nearest fuel station");
				   
			   }
			   if(s1.equals("S"))
			   {
				   System.out.println("Seat belt not fastened");
				   System.out.println("Bitte anschnalen");
			   }
			   if(s1.equals("A"))
			   {
				   System.out.println("Seat belt fastened");
			   }
			}
		}
			}
		
	}
	/**
	 * Helper thread to send the current platoon status from the leader truck to the connected truck
	 * */
	class SendState extends Thread{
		
		final ObjectOutputStream o;
		final Socket s;
		final int ID;
		/**
		 * Initialize the thread
		 * @param s the connected truck socket
		 * @param o the output stream of the socket
		 * @param ID the current index of truck
		 * */
		public SendState(Socket s,ObjectOutputStream o,int ID) {
			this.ID=ID;
			this.s=s;
			this.o=o;			
		}
		public void run() {
				 while (true) {
					try {
						synchronized (Platoon_Status) {
							synchronized (o) {
								o.writeObject(getPlatoonStatus());
								System.out.println("server sending status : "+ getPlatoonStatus());
							}
						}
					}catch(SocketException e) {
						return;
					}
					catch (IOException e) {						
						e.printStackTrace();
						
					}									 					 
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
				}
				
			 
		}
	}
	/**
	 * Helper thread to receive the connected trucks status. Updating the platoon array.
	 * Based on inputs received, determines Platoon status as well
	 * Includes Error Handling option to reconnect to disconnected clients (follower trucks)
	 * */
	class receiveMessage extends Thread{	
		final int ID;
		final ObjectInputStream i;
		final Socket s;
		SingleTruck server;
		SingleTruck receive_truck;
		/**
		 * Initializing the thread
		 * @param s the connected truck socket
		 * @param o the output stream of the socket
		 * @param ID the current truck index
		 * @param truck the server truck instance in main method
		 * */
		public receiveMessage(Socket s,ObjectInputStream i,int ID,SingleTruck truck) {
			this.ID=ID;
			this.s=s;
			this.i=i;
			server=truck;
		}
		public void run() {			
			while (true) {
				int index = 0;
				int occurence = 0;
				int fuel_failure = 0;
				try {

					synchronized(server)
					{
						synchronized (i) {
							receive_truck = (SingleTruck) i.readObject();
							TruckList[ID] = receive_truck;
							/* Check the interrupt state of the server truck , if no interrupts then check for the client interrupt states.
							prio 1 -> Overheat
							prio 2 -> Brake
							Prio 3 -> fuel level low*/
							if(overheat_detected)
							{
								System.out.println("Leading truck detected overheat issue, making the platoon to stop");
								server.setPlatoonStatus(Status.STOP);
							}
							else if(obstacle_detected)
							{
								System.out.println("Leading Truck detected an obstacle, making the platoon to brake");
								server.setPlatoonStatus(Status.BRAKE);
							}
							
							else
							{
								/* Check all the interrupts received from the clients and prioritize the requests : start
							prio 1 -> Overheat
							prio 2 -> Brake
							Prio 3 -> fuel level low

								 */
								for(int i =0; i< TruckList.length; i++)
								{
									if((TruckList[i].getInterrupt_received() == Status.OVERHEAT) || (TruckList[i].getInterrupt_received() == Status.STOP))
									{
										index++;
									}
									else if((TruckList[i].getInterrupt_received() == Status.BRAKE))
									{
										occurence++;
									}
									else if((TruckList[i].getInterrupt_received() == Status.FUEL_DEPLETED))
									{

										fuel_failure++;
									}
								}
								if(index != 0)
								{
									server.setPlatoonStatus(Status.STOP);
								}
								else if(occurence != 0)
								{
									server.setPlatoonStatus(Status.BRAKE);
								}
								else if(fuel_failure !=0)
								{
									System.out.println("Server detected a fuel issue in the platoon, searching for the nearest gas station");
								}
								else
								{
									server.setPlatoonStatus(Status.NORMAL);
								}

								/* Check all the interrupts received from the clients and prioritize the requests : end */
							}
						}
					}
					Thread.sleep(1000);
					continue;					
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
	
	public Status getPlatoonStatus() {
		return Platoon_Status;
	}

	public void setPlatoonStatus(Status state) {
		this.Platoon_Status = state;
	}
	

	public void setlocal_state(Status state) {
		this.local_state = state;
	}
	
	public Status getlocal_state() {
		return local_state;
	}

	public Status getInterrupt_received() {
		return interrupt_received;
	}

	public void setInterrupt_received(Status interrupt_received) {
		this.interrupt_received = interrupt_received;
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

