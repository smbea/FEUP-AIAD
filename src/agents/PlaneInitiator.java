package agents;

import java.util.Vector;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

@SuppressWarnings("serial")
public class PlaneInitiator extends ContractNetInitiator{
	Agent planeInitiator;
	ACLMessage cfp;

	public PlaneInitiator(Agent a, ACLMessage cfp) {
		super(a, cfp);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected String createConvId(Vector arg0) {
		// TODO Auto-generated method stub
		return super.createConvId(arg0);
	}
	
	@Override
	protected Vector prepareCfps(ACLMessage cfp) {
		// TODO Auto-generated method stub
		return super.prepareCfps(cfp);
	}
	
	@Override
	protected void handlePropose(ACLMessage propose, Vector acceptances) {
		// TODO Auto-generated method stub
		super.handlePropose(propose, acceptances);
	}
	
	@Override
	protected void handleAllResponses(Vector responses, Vector acceptances) {
		// TODO Auto-generated method stub
		super.handleAllResponses(responses, acceptances);
		
		/**
		 * activate new CFP-PROPOSE iteration
		 */
		//newIteration(nextMessages);
	}
	
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		// TODO Auto-generated method stub
		super.handleRefuse(refuse);
	}
}
