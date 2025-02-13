package com.platoon;
import java.io.Serializable;


//module dps_truck_platooning {
//	
//}
public class Truck extends Thread implements TruckProperties,Serializable 
{
	
	protected final Role role;	
	private String licensePlate;
	private Mode mode;
	protected Status state;
	private boolean temprature,fuelLevel,seatBelt;
	transient private PlatoonHandler handler;
	
	public Truck(Role role)
	{
		this.role = role;
		handler = new PlatoonHandler(this);
		this.mode = mode.AUTONOMOUS;
		this.state = Status.NORMAL;
		this.temprature = false;
		this.fuelLevel = false;
		this.seatBelt = false;
	}
	
//   public static void main(String [] args) throws InterruptedException {
//	   //Initialization of Connections
//	   Truck truck = null;	   
//	   if(args[0].equalsIgnoreCase("L")) {
//		   truck = new Truck(Role.LEAD);		   
//	   }else if(args[0].equalsIgnoreCase("F")) {
//		   truck = new Truck(Role.FOLLOW);
//	   }
//	   truck.setLicensePlate(args[1]);
//	   truck.setHandler(new PlatoonHandler(truck));
//	   truck.getHandler().createSocket();
//	  	   
//   }
    public void run()
    {
    	
    }

    public String getLicensePlate() {
    	return licensePlate;
    }
    public void setLicensePlate(String lp) {
    	licensePlate = lp;
    }
	public Mode getMode() {
		return mode;
	}


	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public PlatoonHandler getHandler() {
		return handler;
	}

	public void setHandler(PlatoonHandler handler) {
		this.handler = handler;
	}

	public Status getStatus() {
		return state;
	}

	public void setStatus(Status state) {
		this.state = state;
	}
	

	public void setState(Status state) {
		this.state = state;
	}

	public boolean isTemprature() {
		return temprature;
	}

	public void setTemprature(boolean temprature) {
		this.temprature = temprature;
	}

	public boolean isFuelLevel() {
		return fuelLevel;
	}

	public void setFuelLevel(boolean fuelLevel) {
		this.fuelLevel = fuelLevel;
	}

	public boolean isSeatBelt() {
		return seatBelt;
	}

	public void setSeatBelt(boolean seatBelt) {
		this.seatBelt = seatBelt;
	}

	public Role getRole() {
		return role;
	}


}
