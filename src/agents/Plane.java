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
	 * Implements Descentralized Air Traffic, i.e., planes communicate and negotiate
	 * with each other.
	 */
	protected void descentralizedBehaviour() {
		ParallelBehaviour parallel = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
		
		Behaviour movement = new TickerBehaviour(this, (Util.getMovementCost()/speed)*1000) {
				
			@Override
			protected void onTick() {

				if (getActualPos().get("x") == finalPos.get("x") && getActualPos().get("y") == finalPos.get("y")) {
					System.out.println("I " + name + " arrived at destiny");
					finished = true;
					stop();
				}

				if (!finished) {
					try {
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setContent("traffic " + getActualPos().get("x") + " " + getActualPos().get("y"));
						msg.addReceiver(getAID("control"));
						send(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}

					ACLMessage answer = new ACLMessage(ACLMessage.INFORM);
					answer = blockingReceive();
					String s = answer.getContent();
					traffic = Util.refactorTrafficArray(s);
					
					//Util.checkConflict(actualPos, traffic, name);
					
					if (Util.getConflicts().containsKey(name)) {
						conflictPlane = Util.getConflicts().get(name);
						negot = true;
					}

					if (!negot) {
						if (conflictPlane.equals("none")) {
							System.out.println("Plane " + name + " ready to move");
							Util.move(route, getActualPos(), distanceLeft);
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

				if (Util.getConflicts().containsKey(name))
					conflictPlane = Util.getConflicts().get(name);
				

				if (!conflictPlane.equals("none") || Util.getConflicts().containsKey(name)) {
					negot = true;

					addBehaviour(new SimpleBehaviour() {

						@Override
						public boolean done() {
							return !negot;
						}

						@Override
						public void action() {
							if(Util.getInitiator().equals("null"))
								Util.setInitiator(name);
							else
								Util.setResponder(name);
							
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
									
									
									if(Util.getInitiator().equals(name)) {
										System.out.println("Plane: " + name + " is initiator");
									} else if(Util.getResponder().equals(name)) {
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
	 * Implements Centralized Air Traffic, i.e., planes communicate and negotiate
	 * with ATC.
	 */
	protected void centralizedBehaviour() {
		addBehaviour(new SimpleBehaviour() {
			@Override
			public void action() {
				boolean conflict = false;
				
				try {
					ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
					msg.setContent("Request_Move: Agent Plane " + this.getAgent().getLocalName() + " is proposing move from [" + actualPos.get("x") + ", " + actualPos.get("y") + "] according to route");
					msg.addReceiver(getAID("control"));
					send(msg);
					System.out.println(msg.getContent());
				} catch (Exception e) {
					e.printStackTrace();
				}

				ACLMessage answer = new ACLMessage(ACLMessage.INFORM);
				answer = blockingReceive();
				String s = answer.getContent();

				ArrayList<String> proposals = createActionMessages();
				String chosenProposal = evaluateActions(proposals);


				if (s.contentEquals("Conflict")) {
					conflict = true;
					System.out.println("Conflict detected! Agent " + getAgent().getLocalName() + " is starting negotiations...");

					MessageTemplate template = MessageTemplate.and(
							MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
							MessageTemplate.MatchPerformative(ACLMessage.CFP));
					block();
					addBehaviour(new ContractNetResponderAgent(this.getAgent(), template));
				} else {
					Util.move(route, actualPos, distanceLeft);
					try  {
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setContent("Execute_Move: Agent " + getAgent().getLocalName() + "'s Action 'Move to [" + actualPos.get("x") + ", " + actualPos.get("y") + "]' successfully performed");
						msg.addReceiver(getAID("control"));
						send(msg);
						System.out.println(msg.getContent());
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}

			/**
			 * Plane arrived at destiny
			 */
			@Override
			public boolean done() {
				return (actualPos.get("x") == finalPos.get("x") && actualPos.get("y") == finalPos.get("y"));
			}
			});
	}

	protected void setup() {
		argCreation();
		
		if (method.equals("descentralized")) {
			descentralizedBehaviour();
		} else if (method.equals("centralized")) {
			// CODAR BEHAVIOURS CENTRALIZED. MOVE igual ao de cima. Modificar segundo
			// behaviour
			centralizedBehaviour();
		}
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