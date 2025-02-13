
import java.util.Scanner;
import java.io.*;

public class Scan_input extends Thread{
	
	private int set_speed;
	private boolean brake_pedal_pressed;
	private String obstacle;
	private int truck_id;
	
	
	public void get_input() throws IOException
	{
		String input;
		
		BufferedReader scan = new BufferedReader(
		        new InputStreamReader(System.in));
		try 
		{
			
			while(!scan.ready())
			{try {Thread.sleep(1000);}catch(Exception e) {}
			}
			
				
				input = scan.readLine();
				if(input.equals("SPEED"))
				{
					System.out.println("Enter the speed");
					Scanner scan_speed = new Scanner(System.in);
					set_speed = scan_speed.nextInt();
					System.out.println("speed requested");
					brake_pedal_pressed = false;
				}
				else if(input.equals("BRAKE"))
				{
					System.out.println("Enter truck_1 or truck_2 or truck_3");
					Scanner scan_brake = new Scanner(System.in);
					String truck = scan_brake.nextLine();
					System.out.println("Brake requested");
					set_truck_for_braking(truck);
					brake_pedal_pressed = true;
					//truck_id = Integer.parseInt(truck.substring(7));
				}
				else if(input.equals("INTERFERENCE"))
				{
					System.out.println("Enter the number plate info (string)");
					Scanner scan_number_plate = new Scanner(System.in);
					obstacle = scan_number_plate.nextLine();
					System.out.println("obstacle requested");
				}
				else
				{
				}
			
			}
		
		catch(IOException e)
		{
		       System.out.println("ConsoleInputReadTask() cancelled");
		}
	}

	public int get_vehicle_speed()
	{
		return set_speed;
	}

	public boolean get_brake_pedal_sts()
	{
		return brake_pedal_pressed;
	}
	
	public String get_obstacle() {
		
		return obstacle;
	}
	
	public void set_truck_for_braking(String truck1)
	{
		//System.out.println(truck1.charAt(6) );
		truck_id = Integer.parseInt(truck1.substring(6));
	}
	
	public int get_brakepedal_status_for_truck()
	{

		return truck_id;
		
	}
	
	public void reset_brake_pdl_sts()
	{
		brake_pedal_pressed = false;
	}
	
	public void run()
	{
		System.out.println("Checking the start conditions by thread : " + Thread.currentThread().getId());
		System.out.println("Enter SPEED for Setting speed \n BRAKE for pressing the brake \n INTERFERENCE for adding an obstacle");
		while(true)
		{
			try {
				get_input();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//try {Thread.sleep(500);}catch(Exception e) {}
		}
			
	}

}

