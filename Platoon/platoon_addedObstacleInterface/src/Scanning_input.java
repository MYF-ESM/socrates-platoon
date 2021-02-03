import java.util.Scanner;
import java.io.*;

public class Scanning_input extends Thread{
	
	private int set_speed;
	private int brake_pedal_pressed;
	private String obstacle;
	
	
	public void get_input() throws IOException
	{
		String input;
		System.out.println("Checking the start conditions by thread : " + Thread.currentThread().getId());
		System.out.println("Enter SPEED for Setting speed \n BRAKE for pressing the brake \n INTERFERENCE for adding an obstacle");
		BufferedReader scan = new BufferedReader(
		        new InputStreamReader(System.in));
		try 
		{
			while(!scan.ready())
			{try {Thread.sleep(200);}catch(Exception e) {}
			}
			
				
				input = scan.readLine();
				if(input.equals("SPEED"))
				{
					System.out.println("Enter the speed");
					Scanner scan_speed = new Scanner(System.in);
					set_speed = scan_speed.nextInt();
					System.out.println("speed requested");
				}
				else if(input.equals("BRAKE"))
				{
					System.out.println("Brake requested");
					brake_pedal_pressed = 1;
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

	public int get_brake_pedal_sts()
	{
		return brake_pedal_pressed;
	}
	
	public String get_obstacle() {
		
		return obstacle;
	}
	
	public void run()
	{
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
