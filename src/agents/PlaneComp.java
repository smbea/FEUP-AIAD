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
	String[] preferences = {"money", "fuel", "time", "detour"};
	
	public PlaneComp() {}
} 