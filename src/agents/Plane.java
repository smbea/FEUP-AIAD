package agents;

import java.util.*;
import java.util.Map.Entry;

import jade.core.AID;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import protocols.ContractNetResponderAgent;
import utils.*;

@SuppressWarnings("serial")
public class Plane extends Agent {
	private String name;
	private PlanePersonality plane;
	public String conflictPlane = "none";
	private String cidBase;
	private String method;
	protected static int cidCnt = 0;
	private AMSAgentDescription[] agents = null;
	private AID myID;
	private boolean firstIterationOver = false;
	private FSMBehaviour fsm = new FSMBehaviour();
	
	/**
	 * Numerical value that is attached to a particular attribute's level. A higher
	 * value is generally related to more attractiveness.
	 */
	LinkedHashMap<String, LinkedHashMap<Integer, Double>> negotiationAttributes = new LinkedHashMap<>();
	ContractNetResponderAgent responder;
	
	public PlanePersonality getPlane() {
		return plane;
	}
	
	public void setActualPos(Pair<Integer, Integer> actualPos) {
		plane.setActualPos(actualPos);
	}
	
	public void setDistanceLeft(int distanceLeft) {
		plane.setDistanceLeft(distanceLeft);
	}
	
	public LinkedHashMap<String, LinkedHashMap<Integer, Double>> getNegotiationAttributes() {
		return negotiationAttributes;
	}
	
	public void setNegotiationAttributes(LinkedHashMap<String, LinkedHashMap<Integer, Double>> negotiationAttributes) {
		this.negotiationAttributes = negotiationAttributes;
	}
	
	/**
	 * Initialize Plane's Attributes.
	 */
	protected void argCreation() {
		Object[] args = getArguments();
		String[] splited = ((String) args[0]).split(" ");
		Pair<Integer, Integer> actualPos = new Pair<>(Integer.parseInt(splited[1]), Integer.parseInt(splited[2]));
		Pair<Integer, Integer> finalPos = new Pair<>(Integer.parseInt(splited[3]), Integer.parseInt(splited[4]));

		name = getLocalName();
		method = splited[0];
		
		if (name.equals("Coop")) {
			plane = new PlaneCoop();
		} else if (name.equals("Comp")) {
			plane = new PlaneComp();
		}
		

		
		plane.setActualPos(actualPos);
		plane.setFinalPos(finalPos);
		
		initPlaneWeights();
	}

	private void initPlaneWeights() {
		LinkedHashMap<Integer, Double> fuelWeights = ContractNetResponderAgent.generateWeight(plane.getFuelLeft(), 0, 1);
		this.negotiationAttributes.put("fuel", fuelWeights);

		LinkedHashMap<Integer, Double> moneyWeights = ContractNetResponderAgent.generateWeight(plane.getMoneyAvailable(), 0 , 1);
		this.negotiationAttributes.put("money", moneyWeights);

		LinkedHashMap<Integer, Double> timeWeights = ContractNetResponderAgent.generateWeight(plane.getMaxDelay(), 0 , 1);
		this.negotiationAttributes.put("time", timeWeights);

		LinkedHashMap<Integer, Double> detourWeights = ContractNetResponderAgent.generateWeight(plane.getDistanceLeft()*2, 0 , 1);
		this.negotiationAttributes.put("detour", detourWeights);
	}
	
	private void updatePlaneWeights() {
		LinkedHashMap<Integer, Double> fuelWeights = ContractNetResponderAgent.generateWeight(plane.getFuelLeft(), 0, 1);
		this.negotiationAttributes.replace("fuel", fuelWeights);

		LinkedHashMap<Integer, Double> moneyWeights = ContractNetResponderAgent.generateWeight(plane.getMoneyAvailable(), 0 , 1);
		this.negotiationAttributes.replace("money", moneyWeights);

		LinkedHashMap<Integer, Double> timeWeights = ContractNetResponderAgent.generateWeight(plane.getMaxDelay(), 0 , 1);
		this.negotiationAttributes.replace("time", timeWeights);

		LinkedHashMap<Integer, Double> detourWeights = ContractNetResponderAgent.generateWeight(plane.getDistanceLeft()*2, 0 , 1);
		this.negotiationAttributes.replace("detour", detourWeights);
	}
	
	public boolean checkPosition() {
		return (plane.getActualPos().getFirst() == plane.getFinalPos().getFirst() && plane.getActualPos().getSecond() == plane.getFinalPos().getSecond());
	}

	/**
	 * Implements Centralized Air Traffic, i.e., planes communicate and negotiate
	 * with ATC and decentralized Air Traffic, i.e., planes communicate and negotiate
	 * with each other.
	 */
	public void manageBehaviour(String type) {
		if (!checkPosition()) {
			fsm.registerFirstState(moveBehaviour(), "Move State");
			fsm.registerLastState(negotiationBehaviour(type), "Negotiation State");
	
			fsm.registerDefaultTransition("Move State", "Negotiation State");
	
			fsm.registerDefaultTransition("Negotiation State", "Move State");
			
			addBehaviour(fsm);
		}
	}

	protected Behaviour negotiationBehaviour(String type) {
		return (new OneShotBehaviour(this) {
			@Override
			public void action() {
				MessageTemplate template = MessageTemplate.and(
						MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
						MessageTemplate.MatchPerformative(ACLMessage.CFP));

				addBehaviour(new ContractNetResponderAgent(this.getAgent(), template, type));
			}
		});
	}

	protected Behaviour moveBehaviour() {
		return (new TickerBehaviour(this, (Util.getMovementCost() / plane.getSpeed()) * 1000) {
			/**
			 * Plane arrived at destiny
			 */
			public boolean isOver() {
				if (checkPosition()) {
					fsm.deregisterDefaultTransition("Negotiation Behaviour");
					fsm.deregisterDefaultTransition("Move Behaviour");
					fsm.deregisterState("Negotiation Behaviour");
					fsm.deregisterState("Move Behaviour");

					System.out.println("Destination reached for Plane " + getLocalName() + "! Hope you had a good flight :)");
					stop();
					return true;
				}
				if (Util.confirmedConflictCounter == Util.nResponders) {
					stop();
					return true;
				}
				return false;
			}

			@Override
			protected void onTick() {
				if (!isOver()) {
					ACLMessage answer = new ACLMessage(ACLMessage.INFORM);
					answer = blockingReceive();

					if (answer.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
						System.out.println("Conflict detected! Agent " + getAgent().getLocalName()
								+ " is starting negotiations...");

						Util.confirmedConflictCounter++;
					} else if (answer.getPerformative() == ACLMessage.CFP) {
						try {
							ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
							msg.setContent("Request_Route: Agent Plane " + this.getAgent().getLocalName()
									+ " is requesting route from [" + plane.getActualPos().getFirst() + ", " + plane.getActualPos().getSecond()
									+ "] to [" + plane.getFinalPos().getFirst() + ", " + plane.getFinalPos().getSecond() + "]");
							msg.addReceiver(getAID("control"));
							send(msg);
							System.out.println(msg.getContent());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (answer.getPerformative() == ACLMessage.ACCEPT_PROPOSAL && !Util.conflict) {
						if (answer.getContent().contains("Route_Generated")) {
							int index = answer.getContent().indexOf(':');
							int distanceLeft = Integer.parseInt(answer.getContent().substring(index+1));
							plane.setDistanceLeft(distanceLeft);
						}
						Util.move(this.getAgent().getLocalName(), plane);
						try {
							ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.setContent("Execute_Move: Agent " + getAgent().getLocalName() + "'s Action 'Move to ["
									+ plane.getActualPos().getFirst() + ", " + plane.getActualPos().getSecond() + "]' successfully performed");
							msg.addReceiver(getAID("control"));
							send(msg);
							System.out.println(msg.getContent());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					block();

					if (firstIterationOver && !isOver()) {
						try {
							ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
							msg.setContent("Request_Move: Agent Plane " + this.getAgent().getLocalName()
									+ " is proposing move from [" + plane.getActualPos().getFirst() + ", " + plane.getActualPos().getSecond()
									+ "] according to route");
							msg.addReceiver(getAID("control"));
							send(msg);
							System.out.println(msg.getContent());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					firstIterationOver = true;
				}
			}
		});
	}
	
	protected Behaviour moveDecentralizedBehaviour() {
		return (new TickerBehaviour(this, (Util.getMovementCost() / plane.getSpeed()) * 1000) {
			/**
			 * Plane arrived at destiny
			 */
			public boolean isOver() {
				if (Util.confirmedConflictCounter != 0 || (plane.getActualPos().getFirst() == plane.getFinalPos().getFirst() && plane.getActualPos().getSecond() == plane.getFinalPos().getSecond())) {
					stop();
					return true;
				}
				return false;
			}

			@Override
			protected void onTick() {
				if (!isOver()) {
					ACLMessage answer = new ACLMessage(ACLMessage.INFORM);
					answer = blockingReceive();

					if (answer.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
						//System.out.println("Conflict detected! Agent " + getAgent().getLocalName() + " is starting negotiations...");
						
						Util.confirmedConflictCounter++;
						
						System.out.println("Agent = " + getAgent().getLocalName() + ", Conflict counter " + Util.confirmedConflictCounter);
					} else if (answer.getPerformative() == ACLMessage.CFP) {
						try {
							ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
							msg.setContent("Request_Map: Agent Plane " + this.getAgent().getLocalName()
									+ " is proposing move from [" + plane.getActualPos().getFirst() + ", " + plane.getActualPos().getSecond()
									+ "] according to route");
							msg.addReceiver(getAID("control"));
							send(msg);
							System.out.println(msg.getContent());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (answer.getPerformative() == ACLMessage.ACCEPT_PROPOSAL && !Util.conflict) {
						Util.move(this.getAgent().getLocalName(), plane);
						try {
							ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.setContent("Execute_Move: Agent " + getAgent().getLocalName() + "'s Action 'Move to ["
									+ plane.getActualPos().getFirst() + ", " + plane.getActualPos().getSecond() + "]' successfully performed");
							msg.addReceiver(getAID("control"));
							send(msg);
							System.out.println(msg.getContent());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					block();

					if (firstIterationOver && !isOver()) {
						try {
							ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
							msg.setContent("Request_Move: Agent Plane " + this.getAgent().getLocalName()
									+ " is proposing move from [" + plane.getActualPos().getFirst() + ", " + plane.getActualPos().getSecond()
									+ "] according to route");
							msg.addReceiver(getAID("control"));
							send(msg);
							System.out.println(msg.getContent());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					firstIterationOver = true;
				}
			}
		});
	}

	protected void setup() {
		argCreation();
		
		manageBehaviour(method);
	}
}
