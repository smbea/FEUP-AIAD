package agents;

import java.util.Arrays;
import java.util.Date;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import protocols.ContractNetInitiatorAgent;
import utils.Util;

@SuppressWarnings("serial")
public class ATC extends Agent {
	String[][] traffic = new String[5][5];
	boolean comm = false;
	String method;
	private AMSAgentDescription[] agents = null;
	boolean firstIterationOver = false;

	protected void createTraffic() {
		Object[] args = getArguments();
		String s = (String) args[0];
		String[] splitInfo = s.split(" ");
		
		method = splitInfo[splitInfo.length - 1];

		for (int i = 0; i < traffic.length; i++) {
			Arrays.fill(traffic[i], "null");
		}

		for (int i = 0; i < splitInfo.length - 1; i = i + 3) {
			traffic[Integer.parseInt(splitInfo[i + 1])][Integer.parseInt(splitInfo[i + 2])] = (String) splitInfo[i];
		}

	}

	protected void printTraffic() {
		for (int i = 0; i < traffic.length; i++) {
			for (int j = 0; j < traffic[i].length; j++) {
				System.out.print(traffic[i][j] + " ");
			}
			System.out.println();
		}
	}

	protected void centralizedBehaviour() {
		FSMBehaviour fsm = new FSMBehaviour(this);
		
		fsm.registerFirstState(moveBehaviour(), "Move State");
		fsm.registerLastState(negotiationBehaviour(), "Negotiation State");

		fsm.registerDefaultTransition("Move State", "Negotiation State");
		
		addBehaviour(fsm);
	}
	
	protected Behaviour negotiationBehaviour() {
		return (new OneShotBehaviour(this) {
			@Override
			public void action() {
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
				cfp.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

				for (int i = 0; i < agents.length; i++) {
					AID agentID = agents[i].getName();
					if (!agentID.getName().equals(this.getAgent().getName())) {
						cfp.addReceiver(agentID);
					}
				}
				
				addBehaviour(new ContractNetInitiatorAgent(this.getAgent(), cfp));
			}
			
		});
	}

	protected Behaviour moveBehaviour() {
		return new SimpleBehaviour(this) {
			@Override
			public void action() {
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

				if (!firstIterationOver) {
					System.out.println("\n****************************");
					System.out.println("***                      ***");
					System.out.println("***    START FLIGHTS     ***");
					System.out.println("***                      ***");
					System.out.println("****************************\n");
					
					cfp.setContent("Start Flights: Agent " + this.getAgent().getLocalName()
							+ " is requesting planes' movements");
					System.out.println(cfp.getContent());
					for (int i = 0; i < agents.length; i++) {
						AID agentID = agents[i].getName();
						if (!agentID.getName().equals(this.getAgent().getName())) {
							cfp.addReceiver(agentID);
						}
					}
					send(cfp);

					firstIterationOver = true;
				}

				ACLMessage msg = receive();
				if (msg != null) {
					String content = msg.getContent();

					if (msg.getPerformative() == ACLMessage.PROPOSE) {
						System.out.println("Agent " + msg.getSender().getLocalName() + " proposed '" + content + "'");
						int index = content.indexOf("[");
						int finalIndex = content.indexOf("]");
						String[] coord = content.substring(index + 1, finalIndex).split(", ");

						if (!Util.checkConflict(coord, traffic, msg.getSender().getLocalName()) && !Util.conflict) {
							ACLMessage reply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
							reply.setContent("Accepting proposal '" + content + "' from responder "
									+ msg.getSender().getLocalName());
							reply.addReceiver(msg.getSender());
							send(reply);
						} else {
							Util.conflict = true;
							ACLMessage reply = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
							reply.setContent("Conflict");
							for (int i = 0; i < agents.length; i++) {
								AID agentID = agents[i].getName();
								if (!agentID.getName().equals(this.getAgent().getName())) {
									reply.addReceiver(agentID);
								}
							}
							System.out.println("Agent " + getAgent().getLocalName()
									+ ": Conflict detected! Rejecting proposal");
							send(reply);
						}
					} else if (msg.getPerformative() == ACLMessage.INFORM) {
						System.out.println(
								"Agent " + msg.getSender().getName() + " successfully performed the requested action");
						int index = content.indexOf("[");
						int finalIndex = content.indexOf("]");
						String[] coord = content.substring(index + 1, finalIndex).split(", ");

						for (int i = 0; i < traffic.length; i++) {
							for (int j = 0; j < traffic[i].length; j++) {
								if (traffic[i][j] != "null") {
									if (traffic[i][j].equals(msg.getSender().getLocalName())) {
										traffic[i][j] = "null";
									}
								}
							}
						}

						traffic[Integer.parseInt(coord[0])][Integer.parseInt(coord[1])] = msg.getSender()
								.getLocalName();

						Util.printTraffic(traffic);
					} else {
						System.out.println(msg.getContent());
						System.out.println("Not traffic");
					}
				}
			}

			@Override
			public boolean done() {
				return (Util.confirmedConflictCounter == Util.nResponders);
			}
		};
	}

	protected void descentralizedBehaviour() {
		addBehaviour(new CyclicBehaviour() {

			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					String content = msg.getContent();
					if (content.contains("traffic")) {
						ACLMessage reply = new ACLMessage(ACLMessage.INFORM);

						String[] splitMsg = content.split(" ");

						for (int i = 0; i < traffic.length; i++) {
							for (int j = 0; j < traffic[i].length; j++) {
								if (traffic[i][j] != null) {
									if (traffic[i][j].equals(msg.getSender().getLocalName())) {
										traffic[i][j] = null;
									}
								}
							}
						}

						traffic[Integer.parseInt(splitMsg[1])][Integer.parseInt(splitMsg[1])] = msg.getSender()
								.getLocalName();

						String trafficS = Arrays.deepToString(traffic);

						reply.setContent(trafficS);
						reply.addReceiver(msg.getSender());
						send(reply);
						block();
					} else {
						System.out.println("Not traffic");
					}
				}
			}

		});
	}

	protected void setup() {
		createTraffic();

		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults(new Long(-1));
			agents = AMSService.search(this, new AMSAgentDescription(), c);
			for (int index = 0; index < agents.length; index++) {
				if (!agents[index].getName().getLocalName().equals("df") && !agents[index].getName().getLocalName().equals("rma")
						&& !agents[index].getName().getLocalName().equals("ams")
						&& !agents[index].getName().getLocalName().equals(this.getLocalName())) {
					Util.nResponders++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (method.equals("descentralized")) {
			descentralizedBehaviour();
		} else if (method.equals("centralized")) {
			// CODAR ATC PARA centralized. Semelhante ao de cima mas em vez de devolver
			// checka conflito e começa negocição como initiator pedindo aos avios em
			// conflito (responders) propopsals escolhendo depois a melhor

			centralizedBehaviour();
		}
	}
}
