package com.platoon;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFrame;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SingleTruck extends Thread implements TruckProperties,Serializable {
	transient protected final Role role;	
	private String licensePlate;
	transient private Mode mode;
	protected Status interrupt_received;
	protected Status local_state;
	protected Status Platoon_Status;
	private boolean temprature,fuelLevel,seatBelt;
	transient private Socket t_socket;
	//Needed only for Leading Truck
	transient private static int num_of_clients =0;
	transient private static Socket [] Platoon = new Socket[2];	
	transient private static ServerSocket serverSocket;
//	protected Obstacle_distance obstacle;
//	protected Distance distance;
	//private static ArrayList<SingleTruck> TruckList = new ArrayList<SingleTruck>();
	private static SingleTruck[] TruckList = new SingleTruck[2];
	private static boolean obstacle_detected = false;
	private static boolean overheat_detected = false;
	private static boolean fuel_level_low = false;
	static boolean brake_pressed = false;
	//For Handling Abrupt Closure of Sockets
	
	public SingleTruck(Role role)
	{
		this.role = role;
		this.mode = mode.AUTONOMOUS;
		this.Platoon_Status = Status.NORMAL;
		this.local_state = Status.NORMAL;
		this.temprature = false;
		this.fuelLevel = false;
		this.seatBelt = false;
//		this.distance = Distance.INTERPLATOON_DISTANCE;
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
			   System.out.println("Adding to arryalost");
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
		
		while(true)
		{
			Scanner server_interrupt = new Scanner(System.in);
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
//				int count=0;
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
			
			   Thread interrupt = new Thread(new Runnable() {
				   /* Receive interrupt from keyboard to introduce obstacle or fuel level or over heat conditions in the client trucks */
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
							// TODO Auto-generated catch block
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
		SingleTruck server;
		SingleTruck receive_truck;
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
	
//	public Distance getDistance()
//	{
//		return distance;
//	}
//	
//	public void setDistance(Distance d)
//	{
//		distance = d;
//		
//	}

}

