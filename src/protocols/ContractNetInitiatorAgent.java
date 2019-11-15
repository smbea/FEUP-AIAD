package protocols;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import utils.Util;

import java.util.Vector;

import agents.ATC;
import agents.Plane;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

@SuppressWarnings("serial")
public class ContractNetInitiatorAgent extends ContractNetInitiator {
	private Agent agent;
	private ACLMessage cfp;
	private int nResponders;

	public ContractNetInitiatorAgent(Agent a, ACLMessage cfp) {
		super(a, cfp);
		this.agent = a;
		this.cfp = cfp;

		this.cfp.setContent("Start: Agent " + agent.getLocalName() + " is requesting proposals");

		System.out.println(this.cfp.getContent());
	}

	public int getNumberResponders() {
		return nResponders;
	}

	public void setNumberResponders(int nResponders) {
		this.nResponders = nResponders;
	}

	public Vector prepareCfps(ACLMessage cfp) {
		Vector v = new Vector(1);
		v.addElement(this.cfp);
		return v;
	}

	protected void handlePropose(ACLMessage propose) {
		System.out.println("Agent " + propose.getSender().getName() + " proposed '" + propose.getContent() + "'");
	}

	protected void handleRefuse(ACLMessage refuse) {
		System.out.println("Agent " + refuse.getSender().getName() + " refused");
	}

	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// FAILURE notification from the JADE runtime: the receiver
			// does not exist
			System.out.println("Responder does not exist");
		} else {
			System.out.println("Agent " + failure.getSender().getName() + " failed");
		}
		// Immediate failure --> we will not receive a response from this agent
		nResponders--;
	}

	protected void handleAllResponses(Vector responses, Vector acceptances) {
		if (responses.size() < nResponders) {
			// Some responder didn't reply within the specified timeout
			System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
		}
		
		Util.printTraffic(((ATC)agent).getTraffic());

		// Evaluate proposals.
		int bestProposal = -1;
		AID bestProposer = null;
		ACLMessage accept = null;
		Enumeration e = responses.elements();
		while (e.hasMoreElements()) {
			ACLMessage msg = (ACLMessage) e.nextElement();
			
			if (msg.getPerformative() == ACLMessage.PROPOSE) {
				String msgContent = msg.getContent();

				// No conflict
				if (msgContent.indexOf("conflict") == -1) {
					int index = msgContent.indexOf("[");
					int finalIndex = msgContent.indexOf("]");
					String[] coord = msgContent.substring(index + 1, finalIndex).split(", ");

					HashMap<String, Integer> planePos = new HashMap<String, Integer>();
					planePos.put("x", Integer.parseInt(coord[0]));
					planePos.put("y", Integer.parseInt(coord[1]));

					if (Util.checkConflict(planePos, ((ATC)agent).getTraffic(), msg.getSender().getLocalName())) {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						reply.setContent("Agent " + agent.getLocalName() + ": Conflict detected");
						acceptances.addElement(reply);
					} else {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						acceptances.addElement(reply);
						System.out.println("Accepting proposal '" + msgContent + "' from responder " + msg.getSender().getLocalName());
					}
				} else {
					System.out.println("Agent " + agent.getLocalName() + " detected a conflict!");
					/*
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					acceptances.addElement(reply);
					int proposal = Integer.parseInt(msg.getContent());
					if (proposal > bestProposal) {
						bestProposal = proposal;
						bestProposer = msg.getSender();
						accept = reply;
					}
					*/
				}
			}
		}
		// Accept the proposal of the best proposer
		if (accept != null && bestProposer != null) {
			System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
			accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
		}
		
		System.out.println("new iteration");
		// VETOR
		newIteration(acceptances);
	}

	protected void handleInform(ACLMessage inform) {
		String informContent = inform.getContent();
		System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
		
		int index = informContent.indexOf("[");
		int finalIndex = informContent.indexOf("]");
		String[] coord = informContent.substring(index + 1, finalIndex).split(", ");
		
		((ATC)agent).updateMap(inform.getSender().getLocalName(), Integer.parseInt(coord[0]), Integer.parseInt(coord[1]));
	}
	
	

}
