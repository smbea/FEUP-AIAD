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
	int predictedHours;
	int fuelLeft = 50;
	int speed = 100;
	float timeLeft = 40;
	int money=100;
	int startBid=10;
	int inc=20;
	int maxBid=money;
	int minAcceptBid=15;
	String name;
	Queue<String> route = new LinkedList<>(){{add("DDR");add("DDR");add("DDR");add("DDR");}};
	HashMap<String, Integer> actualPos = new HashMap<String, Integer>(){{put("x", 0);put("y", 0);}};
	HashMap<String, Integer> finalPos = new HashMap<String, Integer>(){{put("x", 4);put("y", 4);}};
	String goal="none";								//money, time, fuel, etc
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
	 * Numerical value that is attached to a particular attribute. A higher value is generally related to more attractiveness.
	 */
	HashMap<String, Integer> negotiationAttr = new HashMap<String, Integer>() {{
		put("fuel", 1);
		put("money", 1);
		put("time", 1);
		put("detour", 1);
	}};
	
	public PlaneCoop() {}
} 