package utils;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class PlaneComp extends PlanePersonality
{
	/**
	 * Fuel left (liters)
	 */
	int fuelLeft = 5000;
	/**
	 * Plane average speed of 100km/h (kilometers per hour)
	 */
	int speed = 100;
	int moneyAvailable = 100;
	int bid = 10;

	public PlaneComp() {
		super.fuelLeft = this.fuelLeft;
		super.speed = this.speed;
		super.moneyAvailable = this.moneyAvailable;
		super.bid = this.bid;
		this.actualPos = new HashMap<String, Integer>(){{put("x", 3);put("y", 3);}};
		this.finalPos = new HashMap<String, Integer>(){{put("x", 0);put("y", 0);}};
	}

} 