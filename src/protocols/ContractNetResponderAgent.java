package protocols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.Map.Entry;

import agents.Plane;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import utils.Pair;
import utils.Util;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

@SuppressWarnings("serial")
public class ContractNetResponderAgent extends ContractNetResponder {
	Agent agent;
	MessageTemplate mt;
	String type;
	ArrayList<String> proposals;
	HashMap<String, Integer> nextMove;
	
	public ContractNetResponderAgent(Agent a, MessageTemplate mt, String type) {
		super(a, mt);
		this.agent = a;
		this.mt = mt;
		this.type = type;
		
		Stack<HashMap<String, Integer>> route = Util.routes.get(a.getLocalName());
		if (!route.empty()) {
			nextMove = route.peek();
		} else {
			((Plane)getAgent()).manageBehaviour("centralized");
		}
	}

	/**
	 * Handle initial CFP message.
	 * 
	 * @param cfp call-for-proposal received from initiator
	 * @return proposal
	 */
	@Override
	protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
		System.out.println("Agent " + agent.getLocalName() + ": CFP received from " + cfp.getSender().getName()
				+ ". Action is '" + cfp.getContent() + "'");

		proposals = createActionMessages();
		
		System.out.println("proposals " + proposals);
		
		Pair<String, Double> chosenProposal = evaluateActions(proposals);
		String proposal = chosenProposal.getFirst() + " with Utility=" + chosenProposal.getValue();
	
		if (chosenProposal != null) {
			// We provide a proposal
			System.out.println();
			System.out.println("Agent " + agent.getLocalName() + ": Proposing " + proposal);
			ACLMessage propose = cfp.createReply();
			propose.setPerformative(ACLMessage.PROPOSE);
			propose.setContent(proposal);
			return propose; 
		} else {
			// We refuse to provide a proposal
			System.out.println("Agent " + agent.getLocalName() + ": Refuse");
			throw new RefuseException("evaluation-failed");
		}
	}

	@Override
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
			throws FailureException {
		System.out.println("Agent " + agent.getLocalName() + ": Proposal accepted");
		
		if (performAction(propose.getContent())) {
			ACLMessage inform = accept.createReply();
			inform.setPerformative(ACLMessage.INFORM);
			inform.setContent("Agent " + getAgent().getLocalName() + "'s Action 'Move to ["
					+ ((Plane)getAgent()).getPlane().getActualPos().getFirst() + ", " + ((Plane)getAgent()).getPlane().getActualPos().getSecond() + "]' successfully performed");
			System.out.println(inform.getContent());
			
			Util.confirmedConflictCounter--;
			
			if (Util.confirmedConflictCounter == 0) {
				Util.conflict = false;
			}
			
			((Plane)getAgent()).manageBehaviour("centralized");
			
			return inform;
		} else {
			System.out.println("Agent " + agent.getLocalName() + ": Action execution failed");
			throw new FailureException("unexpected-error");
		}
	}

	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		System.out.println("Agent " + agent.getLocalName() + ": Proposal rejected");
	
		Pair<String, Double> chosenProposal = evaluateActions(proposals);
		String proposal = chosenProposal.getFirst() + " with Utility=" + chosenProposal.getValue();
	
		if (chosenProposal != null) {
			// We provide a proposal
			System.out.println();
			System.out.println("Agent " + agent.getLocalName() + ": Proposing " + proposal);
			ACLMessage reply = reject.createReply();
			reply.setPerformative(ACLMessage.PROPOSE);
			reply.setContent(proposal);
		}
	}

	private boolean performAction(String msg) {
		Pair<Integer, Integer> position = Util.calculatePosition(msg, ((Plane)getAgent()).getPlane().getActualPos());
		
		if (position != null) {
			int distance = ((Plane)getAgent()).getPlane().getDistanceLeft() - 1; 
			
			((Plane)getAgent()).getPlane().setActualPos(position);
			((Plane)getAgent()).getPlane().setDistanceLeft(distance);

			return true;
		}
		
		return false;
	}
	
	/**
	 * For each available action, generate a weight considering the agent's wish to minimize it.
	 * @param max
	 * @param min
	 * @param maxWeight
	 * @return
	 */
	public static LinkedHashMap<Integer, Double> generateWeight(int max, int min, double maxWeight) {
		LinkedHashMap<Integer, Double> weights = new LinkedHashMap<Integer, Double>();

		Random rand = new Random();
		double sum = 0;
		double previousRand;
		double actualRand = maxWeight;

		for(int i = min; i <= max+1; i++) {
			previousRand = actualRand;

			if(i == max+1) {

				for (Map.Entry<Integer, Double> element: weights.entrySet()){
					weights.put(min, element.getValue()+(maxWeight-sum));
					break;
				}

				break;
			}

			do {
				actualRand = 0 + (rand.nextDouble() * (previousRand - 0));
			} while(sum + actualRand > maxWeight);

			sum += actualRand;
			weights.put(i, actualRand);
		}

		return weights;
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
				add("MU");
				add("MD");
				add("MR");
				add("ML");
				add("MDR");
				add("MDL");
				add("MUR");
				add("MUL");
			}
		};

		for (int i = 0; i < possibleMoves.size(); i++) {
			String move = Util.parseMove(possibleMoves.get(i));
			Pair<Integer, Integer> pos = Util.calculatePosition(possibleMoves.get(i), ((Plane)getAgent()).getPlane().getActualPos());
			
			proposal = ((Plane)this.getAgent()).getLocalName() + ": Move " + move;

			if (pos != null && nextMove != null) {
				if (pos.getFirst() == nextMove.get("x") && pos.getSecond() == nextMove.get("y")) {
					proposal += " and Payment " + ((Plane)getAgent()).getPlane().getBid();
				}
			}
			proposals.add(proposal);
		}

		return proposals;
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

	Pair<String, Double> evaluateActions(ArrayList<String> proposals) {

		HashMap<String, Double> currentWeights = this.calculateCurrentWeights();
		Pair<String, Double> bestProposal = new Pair<>(null, Double.NEGATIVE_INFINITY);

		for (String proposal : proposals) {
			HashMap<String, Double> proposalWeights = this.calculateProposalWeights(proposal);
			
			if (proposalWeights != null) {
				double proposalUtility = this.proposalUtility(proposalWeights, currentWeights);
	
				if (bestProposal.getValue() < proposalUtility) {
					bestProposal = new Pair<>(proposal, proposalUtility);
				}
			}
		}
		
		for (int index = 0; index < proposals.size(); index++) {
			if (proposals.get(index).equals(bestProposal.getFirst())) {
				proposals.remove(index);
			}
		}
		
		return bestProposal;
	}

	double calculateStateWeight(String attribute, int value) {
		LinkedHashMap<Integer, Double> attributeWeights =  ((Plane)getAgent()).getNegotiationAttributes().get(attribute);

		for (Integer index: attributeWeights.keySet()) {

			if(Math.ceil(value) == index)
				return attributeWeights.get(index) * value;
		}
		return -1;
	}

	HashMap<String, Double> calculateCurrentWeights() {
		HashMap<String, Double> currentWeights = new HashMap<String, Double>();

		currentWeights.put("fuel", calculateStateWeight("fuel", ((Plane)getAgent()).getPlane().getFuelLeft()));

		currentWeights.put("money", calculateStateWeight("money", ((Plane)getAgent()).getPlane().getMoneyAvailable()));

		currentWeights.put("time", calculateStateWeight("time", ((Plane)getAgent()).getPlane().getTimeLeft()));

		currentWeights.put("detour", calculateStateWeight("detour", ((Plane)getAgent()).getPlane().getDistanceLeft()));

		return currentWeights;
	}

	HashMap<String, Double> calculateProposalWeights(String proposal) {
		int tempMoney = ((Plane)getAgent()).getPlane().getMoneyAvailable();

		if (proposal.contains("Payment")) {
			int paymentIndex = proposal.indexOf("Payment")+8;
			int paidMoney = Integer.parseInt(proposal.substring(paymentIndex));
			
			tempMoney -= paidMoney;
		}

		HashMap<String, Double> proposalWeights = new HashMap<>();

		proposalWeights.put("fuel", calculateStateWeight("fuel", ((Plane)getAgent()).getPlane().getFuelLeft() - ((Plane)getAgent()).getPlane().getFuelLoss()));

		proposalWeights.put("money", calculateStateWeight("money", tempMoney));

		proposalWeights.put("time",  calculateStateWeight("time", ((Plane)getAgent()).getPlane().getTimeLeft() - 1 / ((Plane)getAgent()).getPlane().getSpeed()));

		//new distance left
		int newRouteLength = Util.createPossibleRoute(proposal, ((Plane)getAgent()).getPlane().getActualPos(), ((Plane)getAgent()).getPlane().getFinalPos().getFirst(), ((Plane)getAgent()).getPlane().getFinalPos().getSecond());
		
		if (newRouteLength != -1) {
			this.recalculateDistanceWeights(newRouteLength);
			proposalWeights.put("detour", calculateStateWeight("detour", newRouteLength));
		} else {
			return null;
		}
		
		return proposalWeights;
	}
	
	private void recalculateDistanceWeights(int possibleDistance) {
		LinkedHashMap<Integer, Double> detourWeights = ContractNetResponderAgent.generateWeight(possibleDistance, 0 , 1);
		LinkedHashMap<String, LinkedHashMap<Integer, Double>> negAttrAux = ((Plane)getAgent()).getNegotiationAttributes();
		
		negAttrAux.replace("detour", detourWeights);
		
		((Plane)getAgent()).setNegotiationAttributes(negAttrAux);
	}
}