package agents;

import java.util.Arrays;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class ATC extends Agent {
	String[][] traffic = new String[5][5];
	boolean comm = false;
	
	protected void createTraffic() {
		Object[] args = getArguments();
     	String s = (String) args[0];
     	String[] splitInfo = s.split(" ");
     	
     	for (int i = 0; i < splitInfo.length; i=i+3) {
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
	
	protected void setup() 
    {	
		createTraffic();
		
        addBehaviour(new CyclicBehaviour() {

					@Override
					public void action() {
						ACLMessage msg = receive();
						if(msg != null) {
							String content = msg.getContent();		
							if(content.contains("traffic")) {
								System.out.println("ATC ready to send");
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
	
}
