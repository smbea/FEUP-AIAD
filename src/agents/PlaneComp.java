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
	String[] preferences = new String[4];
	
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
    	name = getLocalName();
    	
    	startBid = 1;
    	inc = 1;
    	maxBid = 10;
    	minAcceptBid = 50;
    	
    	preferences[0] = "money";
    	preferences[1] = "fuel";
    	preferences[2] = "time";
    	preferences[3] = "detour";
   	
    	route.add("DUL");
    	route.add("DUL");
    	route.add("DUL");
	}
	
	protected void setup() 
    {
		argCreation();
		
		ParallelBehaviour parallel = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
		
		Behaviour movement = new TickerBehaviour(this, (Util.movementCost/speed)*1000) {
				
			@Override
			protected void onTick() {
				
				if(actualPos.get("x") == finalPos.get("x") && actualPos.get("y") == finalPos.get("y")) {
					System.out.println("I " + name + " arrived at destiny");
					finished = true;
					stop();
				}
				
				if(!finished) {
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
					
					Util.checkConflict(actualPos, traffic, name);
					
					if (Util.conflicts.containsKey(name)) {
						conflictPlane = Util.conflicts.get(name);
						negot = true;
					}
					
					if (!negot) {
						if(conflictPlane.equals("none")){
							System.out.println("Plane " + name + " ready to move");
							Util.move(route, actualPos);
						} else {
							negot = true;
						}
				
					}
				}
			  }
			};
			
		parallel.addSubBehaviour(movement);
			
		parallel.addSubBehaviour(new SimpleBehaviour() {

			@Override
			public boolean done() {
				return false;
			}

			@Override
			public void action() {

				if (Util.conflicts.containsKey(name))
					conflictPlane = Util.conflicts.get(name);
				

				if (!conflictPlane.equals("none") || Util.conflicts.containsKey(name)) {
					negot = true;

					addBehaviour(new SimpleBehaviour() {

						@Override
						public boolean done() {
							return !negot;
						}

						@Override
						public void action() {
							if(Util.initiator.equals("null"))
								Util.initiator = name;
							else
								Util.responder = name;
							
							ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.setContent("negotiation");
							msg.addReceiver(getAID(conflictPlane));
							send(msg);

							addBehaviour(new SimpleBehaviour() {

								@Override
								public void action() {
									ACLMessage answer = new ACLMessage(ACLMessage.INFORM);
									answer = blockingReceive();
									String s = answer.getContent();
									System.out.println(
											"Plane: " + name + " " + s + " with " + answer.getSender().getLocalName());
									

									if(Util.initiator.equals(name)) {
										System.out.println("Plane: " + name + " is initiator");
									} else if(Util.responder.equals(name)) {
										System.out.println("Plane: " + name + " is responder");
									}

								}

								@Override
								public boolean done() {

									return false;
								}

							});

						}
					});

				}
				comm = true;
			}
		});
		
		addBehaviour(parallel);

    }  
} 