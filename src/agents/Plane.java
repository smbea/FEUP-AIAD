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
public class Plane extends Agent 
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
	String method;
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
		Object[] args = getArguments();
		name=getLocalName();
		method=(String) args[0];
	
		if (name.equals("Coop")) {
			PlaneCoop plane = new PlaneCoop();
			actualPos = plane.actualPos;
			finalPos = plane.finalPos;
			fuelLeft = plane.fuelLeft;
			speed = plane.speed;
			timeLeft = plane.timeLeft;
			money = plane.money;
			goal = plane.goal;
			startBid = plane.startBid;
			inc = plane.inc;
			maxBid = plane.maxBid;
			minAcceptBid = plane.minAcceptBid;
			preferences = plane.preferences;
			route = plane.route;
		} else if (name.equals("Comp")) {
			PlaneComp plane = new PlaneComp();
			actualPos = plane.actualPos;
			finalPos = plane.finalPos;
			fuelLeft = plane.fuelLeft;
			speed = plane.speed;
			timeLeft = plane.timeLeft;
			money = plane.money;
			goal = plane.goal;
			startBid = plane.startBid;
			inc = plane.inc;
			maxBid = plane.maxBid;
			minAcceptBid = plane.minAcceptBid;
			preferences = plane.preferences;
			route = plane.route;
		}
	}
	
	protected void descentralizedBehaviour() {
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
	
	
	protected void centralizedBehaviour() {
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
						msg.setContent("Request_Move " + actualPos.get("x") + " " + actualPos.get("y"));
						msg.addReceiver(getAID("control"));
						send(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}

					ACLMessage answer = new ACLMessage(ACLMessage.INFORM);
					answer = blockingReceive();
					String s = answer.getContent();

					if (s.contentEquals("Conflict")) {
						//negot = true;
						System.out.println("CONFLICT DETECTED IN " + name);
						block();
					} else {
						System.out.println("NO CONFLICT IN " + name);
						System.out.println("Plane " + name + " ready to move");
						Util.move(route, actualPos);
						
						try  {
							ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.setContent("Move " + actualPos.get("x") + " " + actualPos.get("y"));
							msg.addReceiver(getAID("control"));
							send(msg);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				}
			  }
			};
			
			parallel.addSubBehaviour(movement);
			
			Behaviour receiveProposal = new CyclicBehaviour( ) {
				
				@Override
				public void action() {
					ACLMessage msg = receive();
					if(msg != null) {
						String content = msg.getContent();		
						if(content.contains("get-preferences")) {
							
							System.out.println("GET PREFERENCES RECEIVE BY " + name);
							/*ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
							reply.setContent(trafficS);
							reply.addReceiver(msg.getSender());
							send(reply);
							block();
					*/
						}
				};
				}
			};
			
			parallel.addSubBehaviour(receiveProposal);
			
			addBehaviour(parallel);
	}
	
	protected void setup() 
    {
		argCreation();
		
		if(method.equals("descentralized")) {
			descentralizedBehaviour();
		} else if(method.equals("centralized")) {
			//CODAR BEHAVIOURS CENTRALIZED. MOVE igual ao de cima. Modificar segundo behaviour
			centralizedBehaviour();
		}
    }  
}
