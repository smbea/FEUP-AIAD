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
import utils.PlaneComp;
import utils.PlaneCoop;

@SuppressWarnings("serial")
public class Plane extends Agent {
	/**
	 * Fuel Left (liters)
	 */
	private int fuelLeft;
	/**
	 * Predicted Flight Time Left (minutes)
	 */
	private int timeLeft;
	/**
	 * Plane average speed of 100 km/h (kilometers per hour)
	 */
	private int speed;
	/**
	 * Current Distance Left (km)
	 */
	int distanceLeft;
	private int bid;
	private int moneyAvailable;
	String name;
	boolean finished = false;
	boolean comm = false;
	boolean negot = false;
	public String conflictPlane = "none";
	String cidBase;
	String method;
	protected static int cidCnt = 0;
	private HashMap<String, Integer> actualPos = new HashMap<String, Integer>();
	private HashMap<String, Integer> finalPos = new HashMap<String, Integer>();
	String[][] traffic = new String[5][5];
	AMSAgentDescription[] agents = null;
	AID myID;
	int maxDelay;
	HashMap<String, Integer> moveToPos = new HashMap<String, Integer>();
	public boolean conflict = false;
	boolean firstIterationOver = false;
	FSMBehaviour fsm = new FSMBehaviour();
	
	/**
	 * Numerical value that is attached to a particular attribute's level. A higher
	 * value is generally related to more attractiveness.
	 */
	LinkedHashMap<String, LinkedHashMap<Integer, Double>> negotiationAttributes = new LinkedHashMap<>();
	ContractNetResponderAgent responder;
	
	/**
	 * Initialize Plane's Attributes.
	 */
	protected void argCreation() {
		Object[] args = getArguments();
		String[] splited = ((String) args[0]).split(" ");

		name = getLocalName();
		method = splited[0];
		actualPos.put("x", Integer.parseInt(splited[1]));
		actualPos.put("y", Integer.parseInt(splited[2]));
		finalPos.put("x", Integer.parseInt(splited[3]));
		finalPos.put("y", Integer.parseInt(splited[4]));

		if (name.equals("Coop")) {
			PlaneCoop plane = new PlaneCoop();
			initPlaneArgs(plane.getFuelLeft(), plane.getTimeLeft(), plane.getBid(), plane.getDistanceLeft(), plane.getSpeed(), plane.getMoneyAvailable(), plane.getMaxDelay());
			initPlaneWeights(plane, plane.getDistanceLeft());
		} else if (name.equals("Comp")) {
			PlaneComp plane = new PlaneComp();
			initPlaneArgs(plane.getFuelLeft(), plane.getTimeLeft(), plane.getBid(), plane.getDistanceLeft(), plane.getSpeed(), plane.getMoneyAvailable(), plane.getMaxDelay());
			initPlaneWeights(plane, plane.getDistanceLeft());
		}
	}

	private void initPlaneArgs(int fuelLeft, int timeLeft, int bid, int distanceLeft, int speed, int moneyAvailable, int maxDelay) {
		this.fuelLeft = fuelLeft;
		this.timeLeft = timeLeft;
		this.bid = bid;
		this.distanceLeft = distanceLeft;
		this.speed = speed;
		this.moneyAvailable = moneyAvailable;
		this.maxDelay = maxDelay;
	}


	private void initPlaneWeights(PlanePersonality plane, Integer distanceLeft) {
		LinkedHashMap<Integer, Double> fuelWeights = ContractNetResponderAgent.generateWeight(plane.getFuelLeft(), 0, 1);
		this.negotiationAttributes.put("fuel", fuelWeights);

		LinkedHashMap<Integer, Double> moneyWeights = ContractNetResponderAgent.generateWeight(plane.getMoneyAvailable(), 0 , 1);
		this.negotiationAttributes.put("money", moneyWeights);

		LinkedHashMap<Integer, Double> timeWeights = ContractNetResponderAgent.generateWeight(plane.getMaxDelay(), 0 , 1);
		this.negotiationAttributes.put("time", timeWeights);

		LinkedHashMap<Integer, Double> detourWeights = ContractNetResponderAgent.generateWeight(distanceLeft*2, 0 , 1);
		this.negotiationAttributes.put("detour", detourWeights);
	}
	
	public boolean checkPosition() {
		return (actualPos.get("x") == finalPos.get("x") && actualPos.get("y") == finalPos.get("y"));
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
		return (new TickerBehaviour(this, (Util.getMovementCost() / speed) * 1000000) {
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
				System.out.println("haha");
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
									+ " is requesting route from [" + actualPos.get("x") + ", " + actualPos.get("y")
									+ "] to [" + finalPos.get("x") + ", " + finalPos.get("y") + "]");
							msg.addReceiver(getAID("control"));
							send(msg);
							System.out.println(msg.getContent());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (answer.getPerformative() == ACLMessage.ACCEPT_PROPOSAL && !Util.conflict) {
						if (answer.getContent().contains("Route_Generated")) {
							int index = answer.getContent().indexOf(':');
							distanceLeft = Integer.parseInt(answer.getContent().substring(index+1));
							((Plane)getAgent()).setDistanceLeft(distanceLeft);
						}
						Util.move(this.getAgent().getLocalName(), actualPos, distanceLeft);
						try {
							ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.setContent("Execute_Move: Agent " + getAgent().getLocalName() + "'s Action 'Move to ["
									+ actualPos.get("x") + ", " + actualPos.get("y") + "]' successfully performed");
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
									+ " is proposing move from [" + actualPos.get("x") + ", " + actualPos.get("y")
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
		return (new TickerBehaviour(this, (Util.getMovementCost() / speed) * 1000) {
			/**
			 * Plane arrived at destiny
			 */
			public boolean isOver() {
				if (Util.confirmedConflictCounter != 0 || (actualPos.get("x") == finalPos.get("x") && actualPos.get("y") == finalPos.get("y"))) {
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
									+ " is proposing move from [" + actualPos.get("x") + ", " + actualPos.get("y")
									+ "] according to route");
							msg.addReceiver(getAID("control"));
							send(msg);
							System.out.println(msg.getContent());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (answer.getPerformative() == ACLMessage.ACCEPT_PROPOSAL && !Util.conflict) {
						Util.move(this.getAgent().getLocalName(), actualPos, distanceLeft);
						try {
							ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.setContent("Execute_Move: Agent " + getAgent().getLocalName() + "'s Action 'Move to ["
									+ actualPos.get("x") + ", " + actualPos.get("y") + "]' successfully performed");
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
									+ " is proposing move from [" + actualPos.get("x") + ", " + actualPos.get("y")
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

	public HashMap<String, Integer> getActualPos() {
		return actualPos;
	}
	
	public int getDistanceLeft() {
		return distanceLeft;
	}
	
	public void setActualPos(HashMap<String, Integer> actualPos) {
		this.actualPos = actualPos;
	}
	
	public void setDistanceLeft(int distanceLeft) {
		this.distanceLeft = distanceLeft;
	}

	public int getBid() {
		return bid;
	}

	public void setBid(int bid) {
		this.bid = bid;
	}

	public int getFuelLeft() {
		return fuelLeft;
	}

	public void setFuelLeft(int fuelLeft) {
		this.fuelLeft = fuelLeft;
	}
	
	public LinkedHashMap<String, LinkedHashMap<Integer, Double>> getNegotiationAttributes() {
		return negotiationAttributes;
	}
	
	public void setNegotiationAttributes(LinkedHashMap<String, LinkedHashMap<Integer, Double>> negotiationAttributes) {
		this.negotiationAttributes = negotiationAttributes;
	}

	public int getMoneyAvailable() {
		return moneyAvailable;
	}

	public void setMoneyAvailable(int moneyAvailable) {
		this.moneyAvailable = moneyAvailable;
	}

	public int getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(int timeLeft) {
		this.timeLeft = timeLeft;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public HashMap<String, Integer> getFinalPos() {
		return finalPos;
	}

	public void setFinalPos(HashMap<String, Integer> finalPos) {
		this.finalPos = finalPos;
	}

}
