package utils;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class PlaneCoop 
{   
	/**
	 * Fuel Left (liters)
	 */
	private int fuelLeft = 50;
	/**
	 * Plane average speed of 100 km/h (kilometers per hour)
	 */
	private int speed = 100;
	/**
	 * Plane average total fuel loss of 10 L/km (liters per kilometer)
	 */
	private int fuelLoss = 10;
	/**
	 * Predicted Flight Time Left (minutes)
	 */
	private int timeLeft = (int)((double)(fuelLeft/fuelLoss)/speed*60);
	private int bid=10;
	int minAcceptBid=15;
	/**
	 * Maximum delay allowed is 2 times the current estimated flight time
	 */
	int maxDelay = 2*timeLeft;
	/**
	 * Money available to spend
	 */
	private int moneyAvailable = 30;
	/**
	 * Current flight route
	 */
	private Queue<String> route = new LinkedList<>(){{add("DDR");add("DDR");add("DDR");add("DDR");}};
	/**
	 * Current position coordinates
	 */
	private HashMap<String, Integer> actualPos = new HashMap<String, Integer>(){{put("x", 0);put("y", 0);}};
	/**
	 * Destiny position coordinates
	 */
	private HashMap<String, Integer> finalPos = new HashMap<String, Integer>(){{put("x", 4);put("y", 4);}};
	/**
	 * Current Distance Left (km)
	 */
	private int distanceLeft = getRoute().size();
	
	public PlaneCoop() {}

	public HashMap<String, Integer> getActualPos() {
		return actualPos;
	}

	public void setActualPos(HashMap<String, Integer> actualPos) {
		this.actualPos = actualPos;
	}

	public HashMap<String, Integer> getFinalPos() {
		return finalPos;
	}

	public void setFinalPos(HashMap<String, Integer> finalPos) {
		this.finalPos = finalPos;
	}

	public int getFuelLeft() {
		return fuelLeft;
	}

	public void setFuelLeft(int fuelLeft) {
		this.fuelLeft = fuelLeft;
	}

	public int getFuelLoss() {
		return fuelLoss;
	}

	public void setFuelLoss(int fuelLoss) {
		this.fuelLoss = fuelLoss;
	}

	public int getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(int timeLeft) {
		this.timeLeft = timeLeft;
	}

	public int getBid() {
		return bid;
	}

	public void setBid(int bid) {
		this.bid = bid;
	}

	public Queue<String> getRoute() {
		return route;
	}

	public void setRoute(Queue<String> route) {
		this.route = route;
	}

	public int getDistanceLeft() {
		return distanceLeft;
	}

	public void setDistanceLeft(int distanceLeft) {
		this.distanceLeft = distanceLeft;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getMoneyAvailable() {
		return moneyAvailable;
	}

	public void setMoneyAvailable(int moneyAvailable) {
		this.moneyAvailable = moneyAvailable;
	}

	public int getMaxDelay() {
		return maxDelay;
	}
	
	public void setMaxDelay(int maxDelay) {
		this.maxDelay = maxDelay;
	}
} 