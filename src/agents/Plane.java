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
import javafx.util.Pair;
import protocols.ContractNetInitiatorAgent;
import protocols.ContractNetResponderAgent;
import utils.PlaneComp;
import utils.PlaneCoop;
import utils.PlanePersonality;
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
	public String conflictPlane = "none";
	String cidBase;
	String method;
	protected static int cidCnt = 0;
	Queue<String> route = new LinkedList<>();
	private HashMap<String, Integer> actualPos = new HashMap<String, Integer>();
	HashMap<String, Integer> finalPos = new HashMap<String, Integer>();
	String[][] traffic = new String[5][5];
	AMSAgentDescription[] agents = null;
	AID myID;
	int maxDelay;
	HashMap<String, Integer> moveToPos = new HashMap<String, Integer>();
	public boolean conflict = false;
	boolean firstIterationOver = false;

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
		name = getLocalName();
		method = (String) args[0];

		if (name.equals("Coop")) {
			PlaneCoop plane = new PlaneCoop();
			initPlaneArgs(plane.getActualPos(), plane.getFinalPos(), plane.getFuelLeft(), plane.getFuelLoss(), plane.getTimeLeft(), plane.getBid(), plane.getRoute(), plane.getDistanceLeft(), plane.getSpeed(), plane.getMoneyAvailable(), plane.getMaxDelay());
			initPlaneWeights(plane, plane.getDistanceLeft());
		} else if (name.equals("Comp")) {
			PlaneComp plane = new PlaneComp();
			initPlaneArgs(plane.getActualPos(), plane.getFinalPos(), plane.getFuelLeft(), plane.getFuelLoss(), plane.getTimeLeft(), plane.getBid(), plane.getRoute(), plane.getDistanceLeft(), plane.getSpeed(), plane.getMoneyAvailable(), plane.getMaxDelay());
			initPlaneWeights(plane, plane.getDistanceLeft());
		}
	}

	private void initPlaneArgs(HashMap<String, Integer> actualPos, HashMap<String, Integer> finalPos, int fuelLeft, int fuelLoss, int timeLeft, int bid, Queue<String> route, int distanceLeft, int speed, int moneyAvailable, int maxDelay) {
		this.actualPos = actualPos;
		this.finalPos = finalPos;
		this.fuelLeft = fuelLeft;
		this.fuelLoss = fuelLoss;
		this.timeLeft = timeLeft;
		this.bid = bid;
		this.route = route;
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



	protected double proposalUtility(HashMap<String, Double> proposalWeights, HashMap<String, Double> currentWeights) {
		double utility = 0;
		double proposalCost = 0;
		double currentCost  = 0;

		for (Entry<String, Double> weightPair : proposalWeights.entrySet()){
			String category = weightPair.getKey();
			proposalCost += weightPair.getValue();
			currentCost += currentWeights.get(category);
		}

		utility = currentCost - proposalCost;

		return utility;
	}

	/**
	 * 
	 * @param action
	 * @return
	 */
	protected double calcDealCost(HashMap<Integer, Double> action) {
		double sumCost = 0;
/*
		for (Entry<Integer, Double> actionItem : action.entrySet()) {
			sumCost += valueNormalizationFuncion(actionItem.getKey(), min, max) * actionItem.getValue();
		}
*/
		return sumCost;
	}

	String evaluateActions(ArrayList<String> proposals) {

		HashMap<String, Double> currentWeights = this.calculateCurrentWeights();
		Pair<String, Double> bestProposal = new Pair<>(null, 0.0);

		for (String proposal : proposals) {

			HashMap<String, Double> proposalWeights = this.calculateProposalWeights(proposal);
			double proposalUtility = this.proposalUtility(proposalWeights, currentWeights);

			if(bestProposal.getValue() < proposalUtility)
				bestProposal = new Pair<>(proposal, proposalUtility);

		}

		return bestProposal.getKey();
	}

	double calculateStateWeight(String attribute, int value) {
		LinkedHashMap<Integer, Double> attributeWeights =  negotiationAttributes.get(attribute);

		for (Integer index: attributeWeights.keySet()) {

			if(Math.ceil(value) == index)
				return attributeWeights.get(index) * value;
		}
		return -1;
	}

	HashMap<String, Double> calculateCurrentWeights() {
		HashMap<String, Double> currentWeights = new HashMap<String, Double>();

		currentWeights.put("fuel", calculateStateWeight("fuel", fuelLeft));

		currentWeights.put("money", calculateStateWeight("money", moneyAvailable));

		currentWeights.put("time", calculateStateWeight("time", timeLeft));

		currentWeights.put("detour", calculateStateWeight("detour", distanceLeft));

		return currentWeights;
	}

	HashMap<String, Double> calculateProposalWeights(String proposal) {
		int tempMoney = moneyAvailable;

		if (proposal.contains("Payment")) {
			int paymentIndex = proposal.lastIndexOf(' ')+1;
			int paidMoney = Integer.parseInt(proposal.substring(paymentIndex));
			tempMoney -= paidMoney;
		}

		HashMap<String, Double> proposalWeights = new HashMap<>();

		proposalWeights.put("fuel", calculateStateWeight("fuel", fuelLeft - fuelLoss));

		proposalWeights.put("money", calculateStateWeight("money", tempMoney));

		proposalWeights.put("time",  calculateStateWeight("time", timeLeft - 1 / speed));

		proposalWeights.put("detour", calculateStateWeight("detour", distanceLeft + 1));
		//System.out.println("calculateProposalWeights - " + negotiationAttributes.get("detour"));

		return proposalWeights;
	}

	/**
	 * 
	 * @return
	 */
	ArrayList<String> createActionMessages() {
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
			proposal = "Agent " + this.getLocalName() + ": Proposes Move " + possibleMoves.get(i);
			if (possibleMoves.get(i).equals(route.element())) {
				proposal += " and Payment " + bid;
			}
			proposals.add(proposal);
		}

		/*for (int i = 0; i < proposals.size(); i++) {
			System.out.println(proposals.get(i));
		}*/

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

		ArrayList<String> proposals = createActionMessages();
		String chosenProposal = evaluateActions(proposals);
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

	public HashMap<String, Integer> getActualPos() {
		return actualPos;
	}
	
	public int getDistanceLeft() {
		return distanceLeft;
	}
	
	public Queue<String> getRoute() {
		return route;
	}

	public void setActualPos(HashMap<String, Integer> actualPos) {
		this.actualPos = actualPos;
	}
	
	public void setDistanceLeft(int distanceLeft) {
		this.distanceLeft = distanceLeft;
	}
	
	public void setRoute(Queue<String> route) {
		this.route = route;
	}
}
