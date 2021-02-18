package com.platoon;


public class Start_conditions extends Thread {

    boolean doorajar_sts;  // 0 - open , 1-closed
    boolean engine_running_sts;  // 0 - engine stopped, 1-engine running
    boolean seat_belt_sts; // 0 - not fastened, 1 - fastened
     
    int dir_of_motion; // 0- straight, 1- left, 2-right, 3- reverse
    boolean EBS_sts; // emergency braking system status 0- EBS active, 1- EBS inactive
    boolean fuel_level_sts; // 0 - fuel_level_low, 1- fuel_level_high
    // to decide whether this is required or not
    int steering_angle; // if the truck is turning, it detects the angle at which the truck is making a turn
    
    int dist_to_preceding_truck; // denotes distance to preceding truck
    double radar_sensor_input;  // holds the value of the radar sensor input  -- Keyboard interrupt
    double camera_input; // holds the camera input values   --Keyboard interrupt
    double rain_sensor_input; // holds the rain sensor input  --Keyboard interrupt
    boolean communication_failure; // 0- communication failure active; 1 - inactive
    
    Start_conditions(boolean door_open_status,boolean engine_run_sts,boolean seat_belt_status)
    {
    	doorajar_sts = door_open_status;
    	engine_running_sts = engine_run_sts ;
    	seat_belt_sts = seat_belt_status;
    }
    
    public boolean Start_Conditions_check() {
    	boolean ret_val = false;
    	
    	System.out.println("Checking the start conditions by thread : " + Thread.currentThread().getId());
    	if( (doorajar_sts == true) && (engine_running_sts == true) && (seat_belt_sts == true) )
    		ret_val =  true;
    	else
    		ret_val = false;
    	
    	return ret_val;
    }
    
    public void run() {
    	while(true)
    	{
    		//System.out.println(i);
    		Start_Conditions_check();
    		try {Thread.sleep(1000);}catch(Exception e) {}
    	}
    }
}
