//module dps_truck_platooning {
//	
//}

// make it to configure class and make it final variables
public class Truck //extends Thread implements Speed_control 
{
	String role; 
	String number_plate; // to check whether the truck belongs to the platoon
	int destination; // exit point number
	String VIN;
	String mode; //enum
	String camera_input; // holds the camera input values   --Keyboard interrupt
	
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
    	Scanning_input input = new Scanning_input();
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
    
    public String get_number_plate_info()
    {
    	return number_plate;
    }
    
    public String get_VIN()
    {
    	return VIN;
    }
    
    public String getMode()
    {
    	return mode;
    
    }
    
    void init_camera_input(Truck T)
    {
    	camera_input = T.get_number_plate_info();
    }
    
    void obstacle_detected()
    {
    	
    	Scanning_input input = new Scanning_input();
    	String obstacle_info = input.get_obstacle();
    	if(true/*obstacle_info.equals(camera_input)*/)
    	{
    		System.out.println("Not an obstacle");
    	}
    	else 
    	{
    		System.out.println("Obstacle in front of" + role);
    		//TO DO inter-platoon distance increase
    		
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
