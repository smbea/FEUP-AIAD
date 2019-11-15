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
import utils.PlaneComp;
import utils.PlaneCoop;
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
	LinkedHashMap<String, LinkedHashMap<Integer, Double>> negotiationAttributes;
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
			actualPos = plane.getActualPos();
			finalPos = plane.getFinalPos();
			fuelLeft = plane.getFuelLeft();
			fuelLoss = plane.getFuelLoss();
			timeLeft = plane.getTimeLeft();
			bid = plane.getBid();
			route = plane.getRoute();
			distanceLeft = plane.getDistanceLeft();
			speed = plane.getSpeed();
			moneyAvailable = plane.getMoneyAvailable();
			maxDelay = plane.getMaxDelay();
		} else if (name.equals("Comp")) {
			PlaneComp plane = new PlaneComp();
			actualPos = plane.getActualPos();
			finalPos = plane.getFinalPos();
			fuelLeft = plane.getFuelLeft();
			fuelLoss = plane.getFuelLoss();
			timeLeft = plane.getTimeLeft();
			bid = plane.getBid();
			route = plane.getRoute();
			distanceLeft = plane.getDistanceLeft();
			speed = plane.getSpeed();
			moneyAvailable = plane.getMoneyAvailable();
			maxDelay = plane.getMaxDelay();
		}
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

		for (Entry<String, LinkedHashMap<Integer, Double>> attrCriteria : negotiationAttributes.entrySet()) {
			int index = 0;
			double sumWeight = 0;

			for (Entry<Integer, Double> weight : attrCriteria.getValue().entrySet()) {
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

			for (Entry<Integer, Double> weight : attrCriteria.getValue().entrySet()) {
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
	protected double calcDealCost(HashMap<Integer, Double> action) {
		double sumCost = 0;
/*
		for (Entry<Integer, Double> actionItem : action.entrySet()) {
			sumCost += valueNormalizationFuncion(actionItem.getKey(), min, max) * actionItem.getValue();
		}
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
			if ((index = proposal.indexOf("Payment")) != -1) {
				paidMoney = Integer.parseInt(proposal.substring(index + 16));
				tempMoney -= paidMoney;
			}

			HashMap<String, Integer> result = new HashMap<String, Integer>();
			result.put("fuel", fuelLeft - fuelLoss);
			result.put("money", tempMoney);
			result.put("time", timeLeft - 1 / speed);
			result.put("detour", distanceLeft + 1);
/*
			cost -= calcDealCost(result);

			if (cost > maxUtil) {
				maxUtil = cost;
				chosenProposal = proposal;
			}*/
		}

		System.out.println("proposal = " + chosenProposal + ", cost final = " + cost + ", utility = " + utility);
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

		for (int i = 0; i < proposals.size(); i++) {
			System.out.println(proposals.get(i));
		}

		return proposals;
	}

	/**
	 * Implements Descentralized Air Traffic, i.e., planes communicate and negotiate
	 * with each other.
	 */
	protected void descentralizedBehaviour() {
		ParallelBehaviour parallel = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);

		Behaviour movement = new TickerBehaviour(this, (Util.getMovementCost() / speed) * 1000) {

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

					Util.checkConflict(getActualPos(), traffic, name);

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
							if (Util.getInitiator().equals("null"))
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

									if (Util.getInitiator().equals(name)) {
										System.out.println("Plane: " + name + " is initiator");
									} else if (Util.getResponder().equals(name)) {
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
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));

		addBehaviour(new ContractNetResponderAgent(this, template));
			
		/*
		ParallelBehaviour parallel = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);

		Behaviour movement = new TickerBehaviour(this, (Util.getMovementCost() / speed) * 1000) {

			@Override
			protected void onTick() {

				if (actualPos.get("x") == finalPos.get("x") && actualPos.get("y") == finalPos.get("y")) {
					System.out.println("I " + name + " arrived at destiny");
					finished = true;
					stop();
				}

				if (!finished) {
					try {
						ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
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
						// negot = true;
						System.out.println("CONFLICT DETECTED IN " + name);
						block();
					} else {
						System.out.println("NO CONFLICT IN " + name);
						System.out.println("Plane " + name + " ready to move");
						Util.move(route, actualPos, distanceLeft);
						try {
							ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.setContent("Move " + actualPos.get("x") + " " + actualPos.get("y"));
							msg.addReceiver(getAID("control"));
							send(msg);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
			//}
			 
		};

		parallel.addSubBehaviour(movement);

		Behaviour receiveProposal = new CyclicBehaviour() {

			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					String content = msg.getContent();
					if (content.contains("Get_Proposals")) {

						System.out.println("Get Proposals Received By " + name);

						MessageTemplate template = MessageTemplate.and(
								MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
								MessageTemplate.MatchPerformative(ACLMessage.CFP));

						ContractNetResponderAgent responder = new ContractNetResponderAgent(this.getAgent(), template);

						System.out.println("OIIIII");
						
						negotiationAttributes = new LinkedHashMap<String, LinkedHashMap<Integer, Double>>() {
							{
								// minimize amount of money spent
								put("money", responder.generateWeight(moneyAvailable, 0, 0.4));
								// minimize amount of fuel spent
								put("fuel", responder.generateWeight(fuelLeft, distanceLeft * fuelLoss, 0.4));
								// minimize amount of flight time
								put("time", responder.generateWeight(maxDelay, timeLeft, 0.4));
								// minimize detour
								put("detour", responder.generateWeight(fuelLeft / fuelLoss, 0, 0.4));
							}
						};
						
						
						
						createActionMessages();

						// evaluateActions(getActions());
						// responder.prepareResponse(cfp);
						// responder.sendMessage();
					}
				}
				;
			}
		};

		parallel.addSubBehaviour(receiveProposal);

		addBehaviour(parallel);*/
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