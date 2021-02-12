
public class Control_behavior extends Thread {

	public static Scan_input input = new Scan_input();
	private int truck_ids;
	Control_behavior()
	{	
	}
	
	boolean get_brake_pdl_sts()
	{
		return input.get_brake_pedal_sts();
	}
	
	
	void set_brake_pdl_status()
	{
		input.reset_brake_pdl_sts();
	}
	int get_truck_ids_for_Braking()
	{
		truck_ids = 0;
		
		if(input.get_brake_pedal_sts())
		{
			truck_ids = input.get_brakepedal_status_for_truck();
		}
		return truck_ids;
	}
	
	void start_input()
	{
		input.start();
	}
}
