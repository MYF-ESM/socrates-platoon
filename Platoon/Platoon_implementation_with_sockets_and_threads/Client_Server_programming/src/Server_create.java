import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Scanner;

//import com.platoon.Parameters;

public class Server_create extends Thread
{
	private final Truck truck;
	private ServerSocket serverSocket = null;	
	public Socket client_accept;
	
	Server_create(Truck truck)
	{
		this.truck = truck;
	}
	
	public void run() {
		try {
			serverSocket = new ServerSocket(9999);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (true)  
        { 
			client_accept = null;
			try {
				 System.out.println("from Server create : Thread" + Thread.currentThread().getName());
				//serverSocket = new ServerSocket(5056);
				//System.out.println("Truck "+TruckProperties.licensePlate+ "has started the Platoon Server");
				client_accept = serverSocket.accept();
				System.out.println("Truck at "+client_accept.getInetAddress().getHostAddress()+ "has successfully connected to Platoon!");
				Thread T2 = new Instream_Handler(client_accept, truck);
				T2.start();
				//Platoon[0]=this.truck;
			}catch(IOException e){
				System.out.println("Server failed to initiate: "+e.toString());
				//createSocket();
			}
        }
	}
}

//class ClientHandler extends Thread
//{
//    final Socket s;
//	private ObjectOutputStream outStream = null;
//	private ObjectInputStream inStream = null;
//	Parameters signal;
//	Truck truck;
//    
//    public ClientHandler(Socket s, Truck truck)  
//    { 
//    	this.truck = truck;
//        this.s = s; 
//    }
    
//    public void run()  
//    { 
//    	 while (true)  
//         { 
//    		 
//    		 try { 
//    			 System.out.println("from Client Handler : Thread" + Thread.currentThread().getName());
//    			 outStream = new ObjectOutputStream(s.getOutputStream());
//    			 outStream.writeObject(Parameters.BRAKE);
//    			 outStream.flush();
//    			 System.out.println("Value sent from Client"+ Parameters.BRAKE);
//
//    		 }
//    		 catch(Exception e)
//    		 {
//    			 e.printStackTrace();
//    		 }
//         }
//    	 try
//    	 {
//		     outStream.close(); 
//		     inStream.close();
//    	 }
//    	 catch(Exception e)
//    	 {
//    		 e.printStackTrace();
//    	 }
//    }
//    }
class Instream_Handler extends Thread
{
	final Socket s;
	//private ObjectInputStream inStream = null;
	//BufferedReader s_in = null;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	Parameters signal = null;
	Truck truck;
	static boolean apply_brake_to_truck_2;
	static boolean apply_brake_to_truck_3;
	public Instream_Handler(Socket s, Truck truck)  
	{ 
		this.truck = truck;
		this.s = s; 
		try {
			dis = new DataInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream()); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	public void run()  
	{ 

		while(true)
		{
			try {  
//				 System.out.println(" from Instream handler : Thread" + Thread.currentThread().getName());
//				 BufferedReader s_in = new BufferedReader(
//				            new InputStreamReader(s.getInputStream()));
//				//inStream = new ObjectInputStream(s.getInputStream());
//				System.out.println("inside Instream");
//				if(s_in.ready())
//				{					
//				String client_output = s_in.readLine();
//				System.out.println("Client output" +client_output);
//				signal = Parameters.valueOf(client_output); 
//				}
//				else
//				{
//					try {Thread.sleep(1000);}catch(Exception e) {};
//					System.out.println("No Value received at Server");
//				}
//				System.out.println("Inside instream handler");
//				System.out.println(dis.readUTF()); 
//				String str = dis.readUTF();
//				if(str.substring(8, str.length()) == "Brake")
//				{
//					if(str.substring(6) == "2")
//						{
//							dos.writeUTF("Brake");
//						}
//				}
				
				if(dis.readUTF().equals("Truck1: Brake"))
				{
					System.out.println("Truck 1 is braking");
					System.out.println("Sending command to Truck 2 and Truck 3 to brake");
//					dos.writeUTF("Received information");
//					apply_brake_to_truck_2 = true;
//					apply_brake_to_truck_3 = true;
				}
//				if ( (truck.truck_id == 2) && (apply_brake_to_truck_2 == true))
//				{
//					dos.writeUTF("Command to Truck 2 to apply brake");
//					
//					apply_brake_to_truck_2 = false;
//				}
//				
//				if((truck.truck_id == 3)&& (apply_brake_to_truck_3 == true))
//				{
//					dos.writeUTF("Command to Truck 3 to apply brake");
//					
//					apply_brake_to_truck_3 = false;
//				}
				
				
				
                 
//				signal = (Parameters) inStream.readObject();
				

				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
//			
//   		 try {
				Thread.yield();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		}
	}
	
}

