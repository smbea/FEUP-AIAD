package agents;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import protocols.ContractNetInitiatorAgent;
import utils.Util;

@SuppressWarnings("serial")
public class ATC extends Agent {
	String[][] traffic = new String[5][5];
	boolean comm = false;
	String method;
	
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
	
	protected void printTraffic() {
		for (int i = 0; i < traffic.length; i++) {
			for (int j = 0; j < traffic[i].length; j++) {
				System.out.print(traffic[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	protected void centralizedBehaviour() {
		// receive message from plane informing move done
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if(msg != null) {
					String content = msg.getContent();
					System.out.println("msg = " + content);
					ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
					
					String[] splitMsg = content.split(" ");
					if(content.contains("Request_Move")) {
						HashMap<String, Integer> planePos = new HashMap<String, Integer>();
						planePos.put("x", Integer.parseInt(splitMsg[1]));
						planePos.put("y", Integer.parseInt(splitMsg[2]));
						
						System.out.println(planePos);
						
						String conflict = Util.checkConflict(planePos, traffic, msg.getSender().getLocalName());

						String message;
						
						if (conflict.equals("none")) {
							message = "Continue";
							System.out.println("CONTINUE - SAYS ATC");
						} else {
							message = "Conflict";
							System.out.println("CONFLICT DETECTED IN ATC");
						}
						reply.setContent(message);
						reply.addReceiver(msg.getSender());
						send(reply);
						block();
					} else if(content.contains("Move")) {						
						for (int i = 0; i < traffic.length; i++) {
							for (int j = 0; j < traffic[i].length; j++) {
								if(traffic[i][j] != "null") {
									if(traffic[i][j].equals(msg.getSender().getLocalName())) {
										traffic[i][j] = "null";
									}
								}
							}
						}
						
						traffic[Integer.parseInt(splitMsg[1])][Integer.parseInt(splitMsg[1])] = msg.getSender().getLocalName();
						
						System.out.println("Updated grid!");
						
						String cfpContent = "Get_Proposals";
						
						ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
						cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
						cfp.setContent(cfpContent);
						cfp.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
						// add all receivers FUTURE
						cfp.addReceiver(msg.getSender());
						
						ContractNetInitiatorAgent initiator = new ContractNetInitiatorAgent(this.getAgent(), cfp);
						
						initiator.prepareCfps(cfp);
						//initiator.sendMessage();
					}
					else {
						System.out.println("Not traffic");
					}
				}
			}

			
		});
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
	
	protected void setup() 
    {	
		createTraffic();
		
		if(method.equals("descentralized")){
			descentralizedBehaviour();
    	} else if (method.equals("centralized")){
    		//CODAR ATC PARA centralized. Semelhante ao de cima mas em vez de devolver
    		//checka conflito e começa negocição como initiator pedindo aos avios em
    		//conflito (responders) propopsals escolhendo depois a melhor
    		
    		centralizedBehaviour();
    	}
    }
}
