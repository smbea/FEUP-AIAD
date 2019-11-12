package agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetResponder;

public class ContractNetResponderAgent extends ContractNetResponder{

	public ContractNetResponderAgent(Agent a, MessageTemplate mt) {
		super(a, mt);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected ACLMessage handleCfp(ACLMessage cfp) {
		try {
			super.handleCfp(cfp);
		} catch (RefuseException | FailureException | NotUnderstoodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cfp;
	}
	
	@Override
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
		try {
			super.handleAcceptProposal(cfp, propose, accept);
		} catch (FailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return accept;
	}
	
	@Override
	public void registerHandleCfp(Behaviour b) {
		super.registerHandleCfp(b);
	}

	@Override
	public void registerHandleAcceptProposal(Behaviour b) {
		super.registerHandleAcceptProposal(b);
	}

}
