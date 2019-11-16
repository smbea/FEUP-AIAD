package agents;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
	private AMSAgentDescription [] agents = null;
	
	protected void createTraffic() {
		Object[] args = getArguments();
     	String s = (String) args[0];
     	String[] splitInfo = s.split(" ");
     	method = splitInfo[splitInfo.length - 1];
     	
     	for (int i = 0; i < traffic.length; i++) {
     		Arrays.fill(traffic[i], "null");
     	}
     	
     	for (int i = 0; i < splitInfo.length - 1; i=i+3) {
			traffic[Integer.parseInt(splitInfo[i+1])][Integer.parseInt(splitInfo[i+2])] = (String) splitInfo[i];
		}
	}
	
	protected void centralizedBehaviour() {
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		cfp.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
		for (int i = 0; i < agents.length; i++) {
			 AID agentID = agents[i].getName();
			if (!agentID.getName().equals(this.getName())) {
				cfp.addReceiver(agentID);
			}
		}
		
		addBehaviour(new ContractNetInitiatorAgent(this, cfp));
		
		/*
		// receive message from plane informing move done
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if(msg != null) {
					String content = msg.getContent();
					ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
					
					if(content.contains("Request_Move")) {
						System.out.println("Agent " + msg.getSender().getLocalName() + " proposed '" + content + "'");
						int index = content.indexOf("[");
						int finalIndex = content.indexOf("]");
						String[] coord = content.substring(index + 1, finalIndex).split(", ");
						
						if (!Util.checkConflict(coord, traffic, msg.getSender().getLocalName())) {
							reply.setContent("Accepting proposal '" + content + "' from responder " + msg.getSender().getLocalName());
							reply.addReceiver(msg.getSender());
							send(reply);
						} else {
							ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
							cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
							cfp.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
							
							for (int i = 0; i < agents.length; i++) {
								AID agentID = agents[i].getName();
								if (!agentID.getName().equals(this.getAgent().getName())) {
									System.out.println(agentID.getName());
									cfp.addReceiver(agentID);
									reply.addReceiver(agentID);
								}
							}
							
							reply.setContent("Conflict");
							send(reply);
							System.out.println("Agent " + getAgent().getLocalName() + ": Conflict detected");
							block();
	
							addBehaviour(new ContractNetInitiatorAgent(this.getAgent(), cfp));
						}
					} else if(content.contains("Execute_Move")) {	
						System.out.println("Agent " + msg.getSender().getName() + " successfully performed the requested action");
						int index = content.indexOf("[");
						int finalIndex = content.indexOf("]");
						String[] coord = content.substring(index + 1, finalIndex).split(", ");
						
						for (int i = 0; i < traffic.length; i++) {
							for (int j = 0; j < traffic[i].length; j++) {
								if(traffic[i][j] != "null") {
									if(traffic[i][j].equals(msg.getSender().getLocalName())) {
										traffic[i][j] = "null";
									}
								}
							}
						}
						
						traffic[Integer.parseInt(coord[0])][Integer.parseInt(coord[1])] = msg.getSender().getLocalName();

						Util.printTraffic(traffic);
					}
					else {
						System.out.println("Not traffic");
					}
				}
			}
		});*/
	}
	
	protected void descentralizedBehaviour() {
		addBehaviour(new CyclicBehaviour() {

			@Override
			public void action() {
				ACLMessage msg = receive();
				if(msg != null) {
					String content = msg.getContent();		
					if(content.contains("traffic")) {
						ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
						
						String[] splitMsg = content.split(" ");
						
						for (int i = 0; i < traffic.length; i++) {
							for (int j = 0; j < traffic[i].length; j++) {
								if(traffic[i][j] != null) {
									if(traffic[i][j].equals(msg.getSender().getLocalName())) {
										traffic[i][j] = null;
									}
								}
							}
						}
						
						traffic[Integer.parseInt(splitMsg[1])][Integer.parseInt(splitMsg[1])] = msg.getSender().getLocalName();
						
						String trafficS = Arrays.deepToString(traffic);
						
						reply.setContent(trafficS);
						reply.addReceiver(msg.getSender());
						send(reply);
						block();
					}else {
						System.out.println("Not traffic");
					}
				}
			}

			
		});
	}
	
	public void updateMap(String responderName, int posX, int posY) {
		for (int i = 0; i < traffic.length; i++) {
			for (int j = 0; j < traffic[i].length; j++) {
				if(traffic[i][j] != "null") {
					if(traffic[i][j].equals(responderName)) {
						traffic[i][j] = "null";
					}
				}
			}
		}
		
		traffic[posX][posY] = responderName;
	}
	
	@SuppressWarnings("deprecation")
	protected void setup() 
    {	
	    try {
	        SearchConstraints c = new SearchConstraints();
	        c.setMaxResults ( new Long(-1) );
	        agents = AMSService.search( this, new AMSAgentDescription (), c );
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
		createTraffic();
		
		 try {
		        SearchConstraints c = new SearchConstraints();
		        c.setMaxResults ( new Long(-1) );
		        agents = AMSService.search( this, new AMSAgentDescription (), c );
		    }
		    catch (Exception e) {e.printStackTrace();}
		
		if(method.equals("descentralized")){
			descentralizedBehaviour();
    	} else if (method.equals("centralized")){
    		//CODAR ATC PARA centralized. Semelhante ao de cima mas em vez de devolver
    		//checka conflito e começa negocição como initiator pedindo aos avios em
    		//conflito (responders) propopsals escolhendo depois a melhor
    		centralizedBehaviour();
    	}
    }

	public String[][] getTraffic() {
		return traffic;
	}

	public void setTraffic(String[][] traffic) {
		this.traffic = traffic;
	}
}
