package agents;
import java.util.ArrayList;
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
public class PlaneComp extends Agent 
{     
	int predictedHours;
	int fuelLeft;
	int speed;
	float timeLeft;
	int money;
	int startBid;
	int inc;
	int maxBid;
	int minAcceptBid;
	String name;
	boolean finished = false;
	boolean comm = false;
	boolean negot = false;
	String conflictPlane = "none";
	String cidBase;
	protected static int cidCnt = 0;
	Queue<String> route = new LinkedList<>();
	HashMap<String, Integer> actualPos = new HashMap<String, Integer>();
	HashMap<String, Integer> finalPos = new HashMap<String, Integer>();
	String[][] traffic = new String[5][5];
	AMSAgentDescription [] agents = null;
	AID myID;
	String goal;								//money, time, fuel, etc
	String type;                                //competitive, cooperative 
	
	protected void argCreation() {
    	actualPos.put("x", 3);
    	actualPos.put("y", 3);
    	
    	finalPos.put("x", 0);
    	finalPos.put("y", 0);
    	
    	fuelLeft = 50;
    	speed = 100;
    	timeLeft = 60;
    	money = 100;
    	goal = "money";
    	type = "competitive";
    	name = getLocalName();
    	
    	startBid = 1;
    	inc = 1;
    	maxBid = 10;
    	minAcceptBid = 40;
   	
    	route.add("DUL");
    	route.add("DUL");
    	route.add("DUL");
	}
	
	protected void setup() 
    {
		argCreation();
		
		Behaviour movement = new TickerBehaviour(this, (Util.movementCost/speed)*1000) {
			
			@Override
			protected void onTick() {
				
				if(actualPos.get("x") == finalPos.get("x") && actualPos.get("y") == finalPos.get("y")) {
					System.out.println("I " + name + " arrived at destiny");
					finished = true;
					stop();
				} 
				
				if(Util.conflicts.containsKey(name))
					negot = true;
				
				if(name.equals("Comp") && !finished) {
					if (!negot) {
						if(conflictPlane.equals("none")){
							System.out.println("Plane " + name + " ready to move");
							Util.move(route, actualPos);
						} else {
							negot = true;
						}
					}
					
					addBehaviour(new SimpleBehaviour() {
						
						@Override
						public boolean done() {
							return comm;
						}
						
						@Override
						public void action() {
							try {
							ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.setContent("traffic " + actualPos.get("x") + " " + actualPos.get("y"));
							msg.addReceiver(getAID("control"));
							send(msg);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							ACLMessage answer = new ACLMessage(ACLMessage.INFORM);
							answer = blockingReceive();
							String s = answer.getContent();
							traffic = Util.refactorTrafficArray(s);
							
							if(Util.conflicts.containsKey(name)) {
								conflictPlane = Util.conflicts.get(name);
							} else
								conflictPlane = Util.checkConflict(actualPos, traffic, name);
							
							if(!conflictPlane.equals("none") || Util.conflicts.containsKey(name)){
								negot = true;
								
								addBehaviour(new SimpleBehaviour() {
									
									@Override
									public boolean done() {
										return !negot;
									}
									
									@Override
									public void action() {
										ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
										msg.setContent("negotiation");
										msg.addReceiver(getAID(conflictPlane));
										send(msg);
										
										ACLMessage answer = new ACLMessage(ACLMessage.INFORM);
										answer = blockingReceive();
										String s = answer.getContent();
										System.out.println(s + " with " + answer.getSender().getLocalName());
									
									}
								});
								
							}
							comm = true;
						}
					});
				}
			}
		};
		
		addBehaviour(movement);
		
		
    }  
} 