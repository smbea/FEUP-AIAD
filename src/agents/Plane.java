package agents;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map.Entry;

import jade.core.AID;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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
	HashMap<String, Double> negotAttrWeight;
	HashMap<String, Integer> negotiationAttr;
	
	/**
	 * Initialize Plane's Attributes.
	 */
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
			negotiationAttr = plane.negotiationAttr;
			negotAttrWeight = plane.negotAttrWeight;
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
			negotiationAttr = plane.negotiationAttr;
			negotAttrWeight = plane.negotAttrWeight;
			route = plane.route;
		}
	}

	/**
	 * Multi-attribute utility function to evalute negotiation attributes.
	 */
	int utilityFunction() {
		int sumWeight = 0, sumUtil = 0;
		
		if (negotiationAttr.size() != negotAttrWeight.size()) {
			System.out.println("Error: number of negotiation set attributes' importance scores "
					+ "do not match with given weights.");
			return -1;
		}
		
		for (Entry<String, Double> weight : negotAttrWeight.entrySet()) {
			sumUtil += negotiationAttr.get(weight.getKey()) * weight.getValue();
			sumWeight += weight.getValue();
		}
		
		if (sumWeight != 1) {
			System.out.println("Error: negotiation set attributes' weight does not sum up to one.");
			return -1;
		}
		
		return sumUtil;
	}
	/**
	 * Implements Descentralized Air Traffic, i.e., planes communicate and negotiate with each other.
	 */
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
	
	/**
	 * Implements Centralized Air Traffic, i.e., planes communicate and negotiate with ATC.
	 */
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
						if(content.contains("Get_Proposals")) {
							
							System.out.println("Get Proposals Received By " + name);
							/*ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
							reply.setContent(trafficS);
							reply.addReceiver(msg.getSender());
							send(reply);
							block();
					*/
							MessageTemplate template = MessageTemplate.and(
							  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
							  		MessageTemplate.MatchPerformative(ACLMessage.CFP) );
							
							ContractNetResponderAgent responder = new ContractNetResponderAgent(this.getAgent(), template);
							
							//responder.prepareResponse(cfp);
							//responder.sendMessage();
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
