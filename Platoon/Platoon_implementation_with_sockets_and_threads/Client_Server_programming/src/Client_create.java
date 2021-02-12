import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Client_create extends Thread {
	
//	private ObjectOutputStream outStream = null;
//	private ObjectInputStream inStream = null;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	BufferedReader s_in = null;
	Parameters signal = null;
	private Truck truck;
	Control_behavior control1 = new Control_behavior();
	
	
	Client_create(Truck truck) throws IOException
	{
		InetAddress ip = InetAddress.getByName("localhost"); 
		this.truck = truck;
		try {
			Socket s = new Socket(ip, 9999); 
			 dis = new DataInputStream(s.getInputStream());
			 dos = new DataOutputStream(s.getOutputStream()); 
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

//		Thread clientthreads = new Thread();
//		System.out.println("Starting Threads");
//		clientthreads.start();
	}
	
    public void run()  
    { 
    	System.out.println("true");
    	 while (true)  
         { 
    		 
    		 try { 
//    			 System.out.println("from Client Handler : Thread" + Thread.currentThread().getName());
//    			 //outStream = new ObjectOutputStream(s.getOutputStream());
//    			 outStream.writeObject(Parameters.BRAKE);
//    			 outStream.flush();
//    			 System.out.println("Value sent from Client"+ Parameters.BRAKE);
    			 //System.out.println("Inside run");

    			 if(control1.get_brake_pdl_sts() == true)
    			 {
    				if(truck.truck_id == control1.get_truck_ids_for_Braking())
    				{
    					System.out.println("Braking");
    					dos.writeUTF("Truck"+ truck.truck_id + ": Brake"); 
    				}
//    				if(dis.readUTF().equals("Command Received"))
//    				{
//    					control1.set_brake_pdl_status();
//    				}
//    				 
    			 }
//    			 
//    			 if(dis.readUTF().equals("Command to Truck 2 to apply brake"));
//    			 {
//    				 if(truck.truck_id == 2)
//    				 {
//    					 dos.writeUTF("Truck"+ truck.truck_id + ": Brake");
//    				 }
//    			 }
//    			 System.out.println("Communicating truck is " + truck.truck_id);
//    			 String t_id = Integer.toString(truck.truck_id);
    			 
    			 

    		 }
    		 catch(Exception e)
    		 {
    			 e.printStackTrace();
    		 }
    		 
 //   		 try {
    			 Thread.yield();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
         }
    }
	

	

}
