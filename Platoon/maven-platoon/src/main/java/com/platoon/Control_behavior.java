
public class Control_behavior {

    int brake_pedal_sts;  // 0 - brake pedal pressed, 1- brake not pressed
    int vehicle_speed = 0;
    
    
    void accelerate_decelerate()
    {
    	Scan_input input = new Scan_input();
    	brake_pedal_sts = input.get_brake_pedal_sts();
    	if(brake_pedal_sts == 0)
    	{
    		vehicle_speed = input.get_vehicle_speed();    		
    	}
    	else
    	{
    		System.out.println("Vehicle in Standstill");
    		vehicle_speed = 0;
    	}
    }
}
