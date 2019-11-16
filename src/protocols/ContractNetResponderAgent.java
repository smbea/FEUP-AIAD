package protocols;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import utils.Util;
import agents.Plane;

@SuppressWarnings("serial")
public class ContractNetResponderAgent extends ContractNetResponder {
	Agent agent;
	MessageTemplate mt;
	
	public ContractNetResponderAgent(Agent a, MessageTemplate mt) {
		super(a, mt);
		this.agent = a;
		this.mt = mt;
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
		int proposalCode = 0;//Util.evaluateAction(cfp.getContent());
		if (proposalCode == 1) {
			// We provide a proposal
			System.out.println("Agent " + agent.getLocalName() + ": Proposing " + proposalCode);
			ACLMessage propose = cfp.createReply();
			propose.setPerformative(ACLMessage.PROPOSE);
			propose.setContent(String.valueOf(proposalCode));
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
			inform.setContent("Agent " + agent.getLocalName() + ": Action successfully performed");
			System.out.println(inform.getContent());
			return inform;
		} else {
			System.out.println("Agent " + agent.getLocalName() + ": Action execution failed");
			throw new FailureException("unexpected-error");
		}
	}

	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		System.out.println("Agent " + agent.getLocalName() + ": Proposal rejected");
	}

	private boolean performAction(String msg) {
		// Simulate action execution by generating a random number
		return (Math.random() > 0.2);
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
				System.out.println("i " + i + " sum: " + sum + " left: " + (maxWeight-sum));

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
}