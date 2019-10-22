package agents;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;

import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class Plane extends Agent 
{       
	int predictedHours;
	int fuelLeft;
	int speed;
	float timeLeft;
	int money;
	
	Queue<Integer> route = new LinkedList<>(); 
	
	HashMap<String, Integer> actualPos = new HashMap<String, Integer>();
	HashMap<String, Integer> finalPos = new HashMap<String, Integer>();
	
	AMSAgentDescription [] agents = null;
	AID myID;
	ACLMessage msg = null;
	
	String goal;								//money, time, fuel, etc
	String type;                                //competitive, cooperative 
	
	@SuppressWarnings("deprecation")
	protected void setup() 
    {
    	Object[] args = getArguments();
     	String s = (String) args[0];
     	String[] splitInfo = s.split(" ");

    	actualPos.put("x", Integer.parseInt(splitInfo[0]));
    	actualPos.put("y", Integer.parseInt(splitInfo[1]));
    	fuelLeft = Integer.parseInt(splitInfo[2]);
    	speed = Integer.parseInt(splitInfo[3]);
    	timeLeft = Integer.parseInt(splitInfo[4]);
    	money = Integer.parseInt(splitInfo[5]);
    	goal = splitInfo[6];
    	type = splitInfo[7];
    	
    	System.out.println("name: " + myID.getLocalName());
    	System.out.println("x = " + actualPos.get("x") + ", y = " + actualPos.get("y"));
    	System.out.println("fuel left = " + fuelLeft);
    	System.out.println("speed = " + speed);
    	System.out.println("time left = " + timeLeft);
    	System.out.println("money = " + money);
    	System.out.println("goal = " + goal);
    	System.out.println("type = " + type);
    	
        addBehaviour( 
        		new SimpleBehaviour( this ) 
        		{

					@Override
					public void action() {
						
					}

					@Override
					public boolean done() {
						return false;
					}  
        			
        		});
    	}  
} 