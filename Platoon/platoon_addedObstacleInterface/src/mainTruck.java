import java.util.ArrayList;

public class mainTruck {

	public static void main(String[] args) {
		System.out.println("success");
	
		
		ArrayList <Truck> Truck_details= new ArrayList <Truck> ();
		//TO DO - Server socket - check the welcoming msg from the connecting client and then add to the platoon.
		Truck_details.add(new Truck("master", "AB 1010", 10, "Truck-T01"));
		Truck_details.add(new Truck("Slave1", "AB 1011", 9, "Truck-T02"));
		Truck_details.add(new Truck("Slave2", "AB 1012", 9, "Truck-T03"));
		
		
//		Start_conditions condition_master = new Start_conditions(false,false,false);
//		
//		Start_conditions condition_slave_1 = new Start_conditions(false,false,false);
//		
//		Start_conditions condition_slave_2 = new Start_conditions(false,false,false);
//			condition_master.start();
//			try {Thread.sleep(10);}catch(Exception e) {}
//			condition_slave_1.start();
//			try {Thread.sleep(10);}catch(Exception e) {}
//			condition_slave_2.start();
			//try {Thread.sleep(10);}catch(Exception e) {}
		Scanning_input input = new Scanning_input();
		input.start();
		
		for(int i=1;i<3;i++)
		{
			Truck_details.get(i).init_camera_input(Truck_details.get(i-1));
		}
		
		// parallel execution
		if(input.get_obstacle() != "")
		{
			for(int i=1;i<3;i++)
			{
				Truck_details.get(i).obstacle_detected();
			}
			
		}
		
		
		
//		for(Truck t : Truck_details)
//		{
//			t.run();
//		}
		
		
	}
	
	

}
