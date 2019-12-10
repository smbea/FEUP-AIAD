package protocols;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import swinginterface.GraphicInterface;
import swinginterface.GraphicsDemo;
import utils.Pair;
import utils.Util;

import java.util.Vector;

import agents.ATC;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@SuppressWarnings("serial")
public class ContractNetInitiatorAgent extends ContractNetInitiator {
	private Agent agent;
	private ACLMessage cfp;
	private int nResponders = Util.nResponders;
	private int handledResponders = 0;

	public ContractNetInitiatorAgent(Agent a, ACLMessage cfp) {
		super(a, cfp);
		this.agent = a;
		this.cfp = cfp;

		this.cfp.setContent("Start: Agent " + agent.getLocalName() + " is requesting proposals");
		
		Util.negotiation = true;

		System.out.println("\n****************************");
		System.out.println("***                      ***");
		System.out.println("***  START NEGOTIATION   ***");
		System.out.println("***                      ***");
		System.out.println("****************************\n");
		System.out.println(this.cfp.getContent());
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
			System.out.println("Waiting for more proposals....");
		}
		// Evaluate proposals.
		int counter = 0;
		double bestProposal = -1;
		AID bestProposer = null;
		ACLMessage accept = null;
		Enumeration e = responses.elements();
		List<Pair<Integer, Integer>> destinations = new ArrayList<>();
	
		while (e.hasMoreElements()) {
			ACLMessage msg = (ACLMessage) e.nextElement();
			if (msg.getPerformative() == ACLMessage.PROPOSE) {
				Pair<Integer, Integer> dest = Util.calculatePosition(msg.getContent(), Util.findAgentMap(msg.getSender().getLocalName(), ((ATC)this.getAgent()).getTraffic()));
				destinations.add(dest);
				
				counter++;
			}
		}
		
		boolean equilibrium = true;
		
		for (int i = 0; i < destinations.size() - 1; i++) {
			if (destinations.get(i).getFirst() == destinations.get(i+1).getFirst()
				&& destinations.get(i).getSecond() == destinations.get(i).getSecond()) {
				equilibrium = false;
			}
		}
		
		if (equilibrium) {
			e = responses.elements();
			
			while (e.hasMoreElements()) {
				ACLMessage msg = (ACLMessage) e.nextElement();
				if (msg.getPerformative() == ACLMessage.PROPOSE) {
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					acceptances.addElement(reply);
				}
			}
		} else {
			e = responses.elements();
			
			while (e.hasMoreElements()) {
				ACLMessage msg = (ACLMessage) e.nextElement();
				if (msg.getPerformative() == ACLMessage.PROPOSE) {
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					acceptances.addElement(reply);
					
					int index = msg.getContent().indexOf("=");
					double proposal = Double.parseDouble(msg.getContent().substring(index+1));
					if (proposal > bestProposal) {
						bestProposal = proposal;
						bestProposer = msg.getSender();
						accept = reply;
					}
					
					((ATC)getAgent()).manageBehaviour("centralized");
				}
			}
			
			// Accept the proposal of the best proposer
			if (accept != null) {
				System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			}
		}
	}

	protected void handleInform(ACLMessage inform) {
		System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
		int index = inform.getContent().indexOf("[");
		int finalIndex = inform.getContent().indexOf("]");
		String[] coord = inform.getContent().substring(index + 1, finalIndex).split(", ");
		String[][] tempTraffic = ((ATC)getAgent()).getTraffic();
		
		for (int i = 0; i < ((ATC)getAgent()).getTraffic().length; i++) {
			for (int j = 0; j < ((ATC)getAgent()).getTraffic().length; j++) {
				if (((ATC)getAgent()).getTraffic()[i][j] != "null") {
					if (((ATC)getAgent()).getTraffic()[i][j].equals(inform.getSender().getLocalName())) {
						tempTraffic[i][j] = "null";
					}
				}
			}
		}

		if (GraphicsDemo.instance != null && GraphicInterface.started)
			GraphicsDemo.instance.setTraffic(tempTraffic);

		tempTraffic[Integer.parseInt(coord[0])][Integer.parseInt(coord[1])] = inform.getSender().getLocalName();
		((ATC)getAgent()).setTraffic(tempTraffic);
		
		handledResponders++;
		
		if (handledResponders == nResponders) {
			Util.negotiation = false;
			((ATC)getAgent()).manageBehaviour("centralized");
		}
	}
}