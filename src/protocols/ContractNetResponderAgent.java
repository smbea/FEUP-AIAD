package protocols;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
		int proposalCode = Util.evaluateAction(cfp.getContent());
		if (proposalCode == 1) {
			// We provide a request to move
			String proposal = "Agent " + agent.getLocalName() + ": Proposing move to [" + ((Plane)agent).getActualPos().get("x") + ", " + ((Plane)agent).getActualPos().get("y") + "]";
			System.out.println(proposal);
			ACLMessage propose = cfp.createReply();
			propose.setPerformative(ACLMessage.PROPOSE);
			propose.setContent(proposal);
			return propose;
		} else if (proposalCode > 2) {
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
		if (performAction()) {
			System.out.println("Agent " + agent.getLocalName() + ": Action successfully performed");
			ACLMessage inform = accept.createReply();
			inform.setPerformative(ACLMessage.INFORM);
			return inform;
		} else {
			System.out.println("Agent " + agent.getLocalName() + ": Action execution failed");
			throw new FailureException("unexpected-error");
		}
	}

	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		System.out.println("Agent " + agent.getLocalName() + ": Proposal rejected");
	}

	private boolean performAction() {
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
	public LinkedHashMap<Integer, Double> generateWeight(int max, int min, double maxWeight) {
		LinkedHashMap<Integer, Double> weights = new LinkedHashMap<Integer, Double>();
		
		weights.put(min, maxWeight);

		Random rand = new Random();
		double sum = maxWeight;
		double previousRand = maxWeight;
		double actualRand = maxWeight;
		
		for(int i = min + 1; i <= max; i++) {
			while(actualRand >= previousRand || sum + actualRand >= 1) {
				actualRand = rand.nextDouble();
			}
			sum += actualRand;	
			weights.put(i, actualRand);
		}
			
		return weights;
	}
}
