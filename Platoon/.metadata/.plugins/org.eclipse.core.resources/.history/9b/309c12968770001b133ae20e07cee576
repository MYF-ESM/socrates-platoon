import java.util.Scanner;

public class Scan_input extends Thread {
	
	private int set_speed;
	private int brake_pedal_pressed;
	
	
	public void get_input()
	{
		int input;
		System.out.println("Enter 0 for Setting speed, 1 for pressing the brake");
		Scanner scan = new Scanner(System.in);
		while(scan.hasNext())
		{
			input = scan.nextInt();
			if(input == 0)
			{
				System.out.println("Enter the speed");
				Scanner scan_speed = new Scanner(System.in);
				set_speed = scan_speed.nextInt();
			}
			else if(input == 1)
			{
				brake_pedal_pressed = 1;
			}
			else {
				
			}
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
	
	public void run()
	{
		while(true)
		{
			get_input();
			try {Thread.sleep(500);}catch(Exception e) {}
		}
			
	}

}
