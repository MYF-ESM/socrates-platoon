import java.io.IOException;

public class Truck{
	private Role role;
	public static int i = 0;
	public int truck_id;
	//private Server_create handler;
	private Client_create sender;
	
	Truck(Role role) throws IOException
	{
		this.role = role;
		i++;
		truck_id = i;
		

	}
	public void socket_create() throws IOException
	{		
		if(this.role == Role.LEAD)
		{
			System.out.println("Master truck_id is"+ this.truck_id);
			Thread handler = new Server_create(this);
			handler.start();
		}
		else
		{
			Thread sender = new Client_create(this);
			System.out.println("Truck Id is "+truck_id);
			sender.start();
		}
		
	}
	

}
