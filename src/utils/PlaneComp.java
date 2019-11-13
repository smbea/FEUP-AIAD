package utils;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Queue;
import java.util.LinkedList;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class PlaneComp
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
	private int timeLeft = 60;
	private int moneyAvailable = 100;
	private int bid=10;
	private Queue<String> route = new LinkedList<>(){{add("DUL");add("DUL");add("DUL");}};
	private HashMap<String, Integer> actualPos = new HashMap<String, Integer>(){{put("x", 3);put("y", 3);}};
	private HashMap<String, Integer> finalPos = new HashMap<String, Integer>(){{put("x", 0);put("y", 0);}};
	/**
	 * Current Distance Left (km)
	 */
	private int distanceLeft = getRoute().size();
	String goal="money";								//money, time, fuel, etc
	
	/**
	 * Numerical value that is attached to a particular attribute's level. A higher value is generally related to more attractiveness.
	 */
	private HashMap<String, LinkedHashMap<Double, Double>> negotiationAttributes = new HashMap<String, LinkedHashMap<Double, Double>>() {{
		// minimize amount of money spent
		put("money", new LinkedHashMap<Double, Double>() {{
			put(50.0, 0.05); // nadir alternative value
			put(49.0, 0.15);  // upper limit for barely acceptable
			put(1.0, 0.3);   // lower limit of ideal values
			put(0.0, 0.5);    // ideal alternative value
		}});
		// maximize amount of fuel
		put("fuel", new LinkedHashMap<Double, Double>() {{
			put(4000.0, 0.5);          // ideal alternative value
			put(4000.0-getFuelLoss(), 0.3);     // lower limit of ideal values
			put(4000.0-getDistanceLeft()*getFuelLoss()/2, 0.15); // upper limit for barely acceptable value
			put(4000.0-getDistanceLeft()*getFuelLoss(), 0.05); // lowest acceptable value
		}});
		// minimize amount of flight time
		put("time", new LinkedHashMap<Double, Double>() {{
			put(getTimeLeft()/60.0, 0.05);
			put(getTimeLeft()/60/2.0, 0.15);
			put(((getFuelLeft()/getFuelLoss())/getSpeed())/2.0, 0.3);
			put(1.0*(getFuelLeft()/getFuelLoss())/getSpeed(), 0.5);
		}});
		// minimize detour
		put("detour", new LinkedHashMap<Double, Double>() {{
			put(1.0*getFuelLeft()/getFuelLoss(), 0.05);
			put(getFuelLeft()/getFuelLoss()/2.0, 0.15);
			put(1.0, 0.3);
			put(0.0, 0.5);
		}});
	}};
	
	public PlaneComp() {}

	public int getMoneyAvailable() {
		return moneyAvailable;
	}

	public void setMoneyAvailable(int moneyAvailable) {
		this.moneyAvailable = moneyAvailable;
	}

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

	public HashMap<String, LinkedHashMap<Double, Double>> getNegotiationAttributes() {
		return negotiationAttributes;
	}

	public void setNegotiationAttributes(HashMap<String, LinkedHashMap<Double, Double>> negotiationAttributes) {
		this.negotiationAttributes = negotiationAttributes;
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
} 