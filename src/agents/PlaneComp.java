package agents;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Queue;
import java.util.LinkedList;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import utils.Util;

@SuppressWarnings("serial")
public class PlaneComp
{     
	/**
	 * Fuel Left (liters)
	 */
	int fuelLeft = 5000;
	/**
	 * Plane average speed of 100 km/h (kilometers per hour)
	 */
	int speed = 100;
	/**
	 * Plane average total fuel loss of 10 L/km (liters per kilometer)
	 */
	int fuelLoss = 10;
	int moneyAvailable = 100;
	int bid=10;
	Queue<String> route = null;
	/**
	 * Predicted Flight Time Left (minutes)
	 */
	int timeLeft/* = route.size()*Util.getMovementCost()*60/speed*/;
	HashMap<String, Integer> actualPos = new HashMap<String, Integer>(){{put("x", 3);put("y", 3);}};
	HashMap<String, Integer> finalPos = new HashMap<String, Integer>(){{put("x", 0);put("y", 0);}};
	/**
	 * Current Distance Left (km)
	 */
	int distanceLeft/* = route.size()*Util.getMovementCost()*/;
	String goal="money";								//money, time, fuel, etc
	
	/**
	 * Numerical value that is attached to a particular attribute's level. A higher value is generally related to more attractiveness.
	 */
	HashMap<String, LinkedHashMap<Double, Double>> negotiationAttributes = new HashMap<String, LinkedHashMap<Double, Double>>() {{
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
			put(4000.0-fuelLoss, 0.3);     // lower limit of ideal values
			put(4000.0-distanceLeft*fuelLoss/2, 0.15); // upper limit for barely acceptable value
			put(4000.0-distanceLeft*fuelLoss, 0.05); // lowest acceptable value
		}});
		// minimize amount of flight time
		put("time", new LinkedHashMap<Double, Double>() {{
			put(timeLeft/60.0, 0.05);
			put(timeLeft/60/2.0, 0.15);
			put(((fuelLeft/fuelLoss)/speed)/2.0, 0.3);
			put(1.0*(fuelLeft/fuelLoss)/speed, 0.5);
		}});
		// minimize detour
		put("detour", new LinkedHashMap<Double, Double>() {{
			put(1.0*fuelLeft/fuelLoss, 0.05);
			put(fuelLeft/fuelLoss/2.0, 0.15);
			put(1.0, 0.3);
			put(0.0, 0.5);
		}});
	}};
	
	public PlaneComp() {}
} 