package protocols;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import utils.Util;

public class ContractNetResponderAgent extends ContractNetResponder {
	Agent agent;
	MessageTemplate mt;
	
	public ContractNetResponderAgent(Agent a, MessageTemplate mt) {
		super(a, mt);
		this.agent = a;
		this.mt = mt;
	}

	@Override
	protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
		System.out.println("Agent " + agent.getLocalName() + ": CFP received from " + cfp.getSender().getName()
				+ ". Action is " + cfp.getContent());
		int proposal = Util.evaluateAction();
		if (proposal > 2) {
			// We provide a proposal
			System.out.println("Agent " + agent.getLocalName() + ": Proposing " + proposal);
			ACLMessage propose = cfp.createReply();
			propose.setPerformative(ACLMessage.PROPOSE);
			propose.setContent(String.valueOf(proposal));
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
}
