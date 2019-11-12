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
	int predictedHours;
	int fuelLeft = 50;
	int speed = 100;
	float timeLeft = 60;
	int money=100;
	int startBid=1;
	int inc=1;
	int maxBid=10;
	int minAcceptBid=50;
	String name;
	Queue<String> route = new LinkedList<>(){{add("DUL");add("DUL");add("DUL");}};
	HashMap<String, Integer> actualPos = new HashMap<String, Integer>(){{put("x", 3);put("y", 3);}};
	HashMap<String, Integer> finalPos = new HashMap<String, Integer>(){{put("x", 0);put("y", 0);}};
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
	 * Numerical value that is attached to a particular attribute. A higher value is generally related to more attractiveness.
	 */
	HashMap<String, Integer> negotiationAttr = new HashMap<String, Integer>() {{
		put("money", 5);
		put("fuel", 1);
		put("time", 1);
		put("detour", 1);
	}};
	
	public PlaneComp() {}
} 