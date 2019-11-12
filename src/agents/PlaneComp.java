package agents;
import java.util.HashMap;
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
	int fuelLeft = 50;
	/**
	 * Plane average speed of 100 km/h (kilometers per hour)
	 */
	int speed = 100;
	/**
	 * Plane average total fuel loss of 10 L/km (liters per kilometer)
	 */
	int fuelLoss = 10;
	/**
	 * Predicted Flight Time Left (minutes)
	 */
	int timeLeft = 60;
	int moneyAvailable = 100;
	int bid=10;
	Queue<String> route = new LinkedList<>(){{add("DUL");add("DUL");add("DUL");}};
	HashMap<String, Integer> actualPos = new HashMap<String, Integer>(){{put("x", 3);put("y", 3);}};
	HashMap<String, Integer> finalPos = new HashMap<String, Integer>(){{put("x", 0);put("y", 0);}};
	/**
	 * Current Distance Left (km)
	 */
	int distanceLeft = route.size();
	String goal="money";								//money, time, fuel, etc
	/**
	 * Importance score of each attribute such that all attribute weights add up to one. A higher score is generally related to more importance.
	 */
	HashMap<String, Double> negotAttrWeight = new HashMap<String, Double>() {{
		put("money", 0.5);
		put("fuel", 0.25);
		put("time", 0.15);
		put("detour", 0.1);
	}};
	
	/**
	 * Numerical value that is attached to a particular attribute's level. A higher value is generally related to more attractiveness.
	 */
	HashMap<String, HashMap<String, Integer>> negotiationAttrLevels = new HashMap<String, HashMap<String, Integer>>() {{
		put("money", new HashMap<String, Integer>() {{
			put("min", 0);
			put("max", 100);
		}});
		put("fuel", new HashMap<String, Integer>() {{
			put("min", 4000);
			put("max", distanceLeft*fuelLoss);
		}});
		put("time", new HashMap<String, Integer>() {{
			put("min", timeLeft/60);
			put("max", (fuelLeft/fuelLoss)/speed);
		}});
		put("detour", new HashMap<String, Integer>() {{
			put("min", 0);
			put("max", fuelLeft/fuelLoss);
		}});
	}};
	
	public PlaneComp() {}
} 