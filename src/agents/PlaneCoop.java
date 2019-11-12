package agents;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class PlaneCoop 
{     
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
	int timeLeft = 40;
	int startBid=10;
	int inc=20;
	int maxBid=100;
	int minAcceptBid=15;
	Queue<String> route = new LinkedList<>(){{add("DDR");add("DDR");add("DDR");add("DDR");}};
	HashMap<String, Integer> actualPos = new HashMap<String, Integer>(){{put("x", 0);put("y", 0);}};
	HashMap<String, Integer> finalPos = new HashMap<String, Integer>(){{put("x", 4);put("y", 4);}};
	/**
	 * Current Distance Left (km)
	 */
	int distanceLeft = route.size();
	/**
	 * Importance score of each attribute such that all attribute weights add up to one. A higher score is generally related to more importance.
	 */
	HashMap<String, Double> negotAttrWeight = new HashMap<String, Double>() {{
		put("fuel", 0.5);
		put("money", 0.25);
		put("time", 0.15);
		put("detour", 0.1);
	}};
	/**
	 * Numerical value that is attached to a particular attribute's level. A higher value is generally related to more attractiveness.
	 */
	HashMap<String, HashMap<String, Integer>> negotiationAttrLevels = new HashMap<String, HashMap<String, Integer>>() {{
		put("money", new HashMap<String, Integer>() {{
			put("min", 0);
			put("max", 1);
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
	
	public PlaneCoop() {}
} 