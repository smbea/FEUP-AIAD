package utils;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class PlaneCoop extends PlanePersonality
{
	/**
	 * Fuel left (liters)
	 */
	int fuelLeft = 5000;
	/**
	 * Plane average speed of 50km/h (kilometers per hour)
	 */
	int speed = 50;
	int bid = 10;
	int moneyAvailable = 20;

	public PlaneCoop() {
		super.fuelLeft = this.fuelLeft;
		super.speed = this.speed;
		super.moneyAvailable = this.moneyAvailable;
		super.bid = this.bid;
		this.actualPos = new HashMap<String, Integer>(){{put("x", 0);put("y", 0);}};
		this.finalPos = new HashMap<String, Integer>(){{put("x", 4);put("y", 4);}};
	}

} 