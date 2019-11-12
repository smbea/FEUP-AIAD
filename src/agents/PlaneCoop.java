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
	String[] preferences = {"fuel", "money", "time", "detour"};
	
	public PlaneCoop() {}
} 