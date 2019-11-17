package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import protocols.ContractNetResponderAgent;
import utils.Util;

@SuppressWarnings("serial")
public class Plane extends Agent {
	/**
	 * Fuel Left (liters)
	 */
	int fuelLeft;
	/**
	 * Plane average total fuel loss of 10 L/km (liters per kilometer)
	 */
	int fuelLoss;
	/**
	 * Predicted Flight Time Left (minutes)
	 */
	int timeLeft;
	/**
	 * Plane average speed of 100 km/h (kilometers per hour)
	 */
	int speed;
	/**
	 * Current Distance Left (km)
	 */
	int distanceLeft;
	int bid;
	int moneyAvailable;
	String name;
	boolean finished = false;
	boolean comm = false;
	boolean negot = false;
	String conflictPlane = "none";
	String cidBase;
	String method;
	protected static int cidCnt = 0;
	Queue<String> route = new LinkedList<>();
	Queue<String> currentPath = new LinkedList<>();
	HashMap<String, Integer> actualPos = new HashMap<String, Integer>();
	HashMap<String, Integer> finalPos = new HashMap<String, Integer>();
	String[][] traffic = new String[5][5];
	AMSAgentDescription[] agents = null;
	AID myID;
	HashMap<String, LinkedHashMap<Double, Double>> negotiationAttributes;
	public boolean conflict = false;
	boolean firstIterationOver = false;

	/**
	 * Initialize Plane's Attributes.
	 */
	protected void argCreation() {
		Object[] args = getArguments();
		name = getLocalName();
		method = (String) args[0];

		if (name.equals("Coop")) {
			PlaneCoop plane = new PlaneCoop();
			actualPos = plane.actualPos;
			finalPos = plane.finalPos;
			fuelLeft = plane.fuelLeft;
			fuelLoss = plane.fuelLoss;
			timeLeft = plane.timeLeft;
			bid = plane.bid;
			negotiationAttributes = plane.negotiationAttributes;
			route = plane.route;
			distanceLeft = plane.distanceLeft;
			speed = plane.speed;
			moneyAvailable = plane.moneyAvailable;
		} else if (name.equals("Comp")) {
			PlaneComp plane = new PlaneComp();
			actualPos = plane.actualPos;
			finalPos = plane.finalPos;
			fuelLeft = plane.fuelLeft;
			fuelLoss = plane.fuelLoss;
			timeLeft = plane.timeLeft;
			bid = plane.bid;
			negotiationAttributes = plane.negotiationAttributes;
			route = plane.route;
			distanceLeft = plane.distanceLeft;
			speed = plane.speed;
			moneyAvailable = plane.moneyAvailable;
		} else if (name.equals("Test")) {
			PlaneTest plane = new PlaneTest();
			actualPos = plane.actualPos;
			finalPos = plane.finalPos;
			fuelLeft = plane.fuelLeft;
			fuelLoss = plane.fuelLoss;
			timeLeft = plane.timeLeft;
			bid = plane.bid;
			negotiationAttributes = plane.negotiationAttributes;
			route = plane.route;
			distanceLeft = plane.distanceLeft;
			speed = plane.speed;
			moneyAvailable = plane.moneyAvailable;
		}
	}
	
	public HashMap<String, Integer> getActualPos() {
		return actualPos;
	}
	
	public HashMap<String, Integer> getFinalPos() {
		return finalPos;
	}

	/**
	 * Update dynamic numerical values of negotiation attributes.
	 */
	void updateNegotiationAttrLevels() {
		negotiationAttributes.remove("fuel");
		negotiationAttributes.remove("time");
		negotiationAttributes.remove("detour");

		// maximize amount of fuel
		negotiationAttributes.put("fuel", new LinkedHashMap<Double, Double>() {
			{
				put(4000.0, 0.5); // ideal alternative value
				put(4000.0 - fuelLoss, 0.3); // lower limit of ideal values
				put(4000.0 - distanceLeft * fuelLoss / 2, 0.15); // upper limit for barely acceptable value
				put(4000.0 - distanceLeft * fuelLoss, 0.05); // lowest acceptable value
			}
		});
		// minimize amount of flight time
		negotiationAttributes.put("time", new LinkedHashMap<Double, Double>() {
			{
				put(timeLeft / 60.0, 0.05);
				put(timeLeft / 60 / 2.0, 0.15);
				put(((fuelLeft / fuelLoss) / speed) / 2.0, 0.3);
				put(1.0 * (fuelLeft / fuelLoss) / speed, 0.5);
			}
		});
		// minimize detour
		negotiationAttributes.put("detour", new LinkedHashMap<Double, Double>() {
			{
				put(1.0 * fuelLeft / fuelLoss, 0.05);
				put(fuelLeft / fuelLoss / 2.0, 0.15);
				put(1.0, 0.3);
				put(0.0, 0.5);
			}
		});
	}

	double valueNormalizationFuncion(double value, double min, double max) {
		return (value - min) / (max - min);
	}

	/**
	 * Multi-attribute utility function to evalute negotiation attributes.
	 */
	protected double utilityFunction() {
		double sumUtil = 0;
		double min = 0, max = 0;

		for (Entry<String, LinkedHashMap<Double, Double>> attrCriteria : negotiationAttributes.entrySet()) {
			int index = 0;
			double sumWeight = 0;

			for (Entry<Double, Double> weight : attrCriteria.getValue().entrySet()) {
				if (index == 0) {
					min = weight.getValue();
				} else if (index == negotiationAttributes.get(attrCriteria.getKey()).size() - 1) {
					max = weight.getValue();
				}
				sumWeight += weight.getValue();
				System.out.println("weight = " + weight.getKey() + " sum = " + sumWeight);
				System.out.println("size = " + negotiationAttributes.get(attrCriteria.getKey()).size());
			}

			System.out.println("attr = " + attrCriteria.getKey() + " , before print " + sumWeight);

			if (sumWeight != 1.0) {
				System.out.println("Error: negotiation set attributes' weight does not sum up to one.");
				return -1;
			}

			for (Entry<Double, Double> weight : attrCriteria.getValue().entrySet()) {
				sumUtil += valueNormalizationFuncion(weight.getKey(), min, max) * weight.getValue();
			}
		}

		return sumUtil;
	}

	/**
	 * 
	 * @param negotiationAttr
	 * @return
	 */
	protected double calcDealCost(HashMap<Double, Double> negotiationAttr) {
		double sumCost = 0;

		/*
		 * for (Entry<Double, Double> weight : negotiationAttr.entrySet()) { sumCost +=
		 * valueNormalizationFuncion(weight.getKey(), min, max)*weight.getValue(); }
		 */

		return sumCost;
	}

	void evaluateActions(ArrayList<String> proposals) {
		int tempMoney = moneyAvailable;
		double utility = utilityFunction();
		double cost = utility;
		int maxUtil = 0;
		String chosenProposal = null;

		for (String proposal : proposals) {
			cost = utility;
			int index;
			int paidMoney;
			HashMap<String, Integer> result = new HashMap<String, Integer>();

			if ((index = proposal.indexOf("Propose_Payment")) != -1) {
				paidMoney = Integer.parseInt(proposal.substring(index + 16));
				tempMoney -= paidMoney;
			} else {
				result.put("detour", distanceLeft + 1);
			}

			result.put("fuel", fuelLeft - fuelLoss);
			result.put("money", tempMoney);
			result.put("time", timeLeft - Util.getMovementCost() * 60 / speed);

			/*
			 * cost -= calcDealCost(result);
			 * 
			 * if (cost > maxUtil) { maxUtil = cost; chosenProposal = proposal; }
			 */
		}

		System.out.println("proposal = " + chosenProposal + ", cost final = " + cost + ", utility = " + utility);
	}

	/**
	 * 
	 * @return
	 */
	ArrayList<String> getActions() {
		ArrayList<String> proposals = new ArrayList<String>();
		String proposal;
		ArrayList<String> possibleMoves = new ArrayList<String>() {
			{
				add("U");
				add("D");
				add("R");
				add("L");
				add("DDR");
				add("DDL");
				add("DUR");
				add("DUL");
			}
		};

		for (int i = 0; i < possibleMoves.size(); i++) {
			proposal = "Propose_Move " + possibleMoves.get(i);
			if (possibleMoves.get(i).equals(route.element())) {
				proposal += ", Propose_Payment " + bid;
			}
			proposals.add(proposal);
		}

		for (int i = 0; i < proposals.size(); i++) {
			System.out.println(proposals.get(i));
		}

		return proposals;
	}

	/**
	 * Implements Centralized Air Traffic, i.e., planes communicate and negotiate
	 * with ATC and decentralized Air Traffic, i.e., planes communicate and negotiate
	 * with each other.
	 */
	protected void manageBehaviour(String type) {
		FSMBehaviour fsm = new FSMBehaviour(this);

		fsm.registerFirstState(moveBehaviour(), "Move State");
		fsm.registerLastState(negotiationBehaviour(type), "Negotiation State");

		fsm.registerDefaultTransition("Move State", "Negotiation State");

		addBehaviour(fsm);
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
		return (new TickerBehaviour(this, (Util.getMovementCost() / speed) * 1000) {
			/**
			 * Plane arrived at destiny
			 */
			public boolean isOver() {
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
									+ " is requesting route from [" + actualPos.get("x") + ", " + actualPos.get("y")
									+ "] to [" + finalPos.get("x") + ", " + finalPos.get("y") + "]");
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
	
	protected Behaviour moveDecentralizedBehaviour() {
		return (new TickerBehaviour(this, (Util.getMovementCost() / speed) * 1000) {
			/**
			 * Plane arrived at destiny
			 */
			public boolean isOver() {
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
}
