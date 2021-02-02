//module dps_truck_platooning {
//	
//}
public class Truck extends Thread implements Speed_control 
{
	String role; 
	String number_plate;
	int destination; // exit point number
	String VIN;
	
	Truck(String role1, String number_plate1, int destination1, String VIN1)
	{
		role = role1;
		number_plate= number_plate1;
		destination = destination1;	
		VIN = VIN1;
	}
	
    public void accelerate_decelerate()
    {
        int brake_pedal_sts;  // 0 - brake pedal pressed, 1- brake not pressed
        int vehicle_speed = 0;
    	Scan_input input = new Scan_input();
    	brake_pedal_sts = input.get_brake_pedal_sts();
    	if(brake_pedal_sts == 0)
    	{
    		vehicle_speed = input.get_vehicle_speed();  
    		System.out.println("Vehicle speed is "+vehicle_speed);
    
    		
    	}
    	else
    	{
    		System.out.println("Vehicle in Standstill");
    		vehicle_speed = 0;
    	}
    }
    
    public void run()
    {
    	while(true)
    	{
    		accelerate_decelerate();
    		
    		
    		try {Thread.sleep(1000);}catch(Exception e) {}
    	}
    }
}
