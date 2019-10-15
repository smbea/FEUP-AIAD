package agents;
import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class Plane extends Agent 
{       
	int flightHours;
	int fuelLeft;
	AMSAgentDescription [] agents = null;
	AID myID;
	ACLMessage msg = null;
	
	@SuppressWarnings("deprecation")
	protected void setup() 
    {
    	Object[] args = getArguments();
     	String s = (String) args[0];
     	String[] split = s.split(" ");

    	flightHours = Integer.parseInt(split[0]);
    	fuelLeft = Integer.parseInt(split[1]);
    	
    	try {
    		SearchConstraints c = new SearchConstraints();
    		c.setMaxResults(new Long(-1));
    		agents = AMSService.search(this, new AMSAgentDescription (), c);
    	} catch (Exception e) {
    		
    	}
    	
    	myID = getAID();
    	
        addBehaviour( 

            new SimpleBehaviour( this ) 
            {
                int n=0;
                
                public void action() 
                {	
                	String name = getLocalName();

                	if(name.equals("JESUS2") && msg == null) {
                		
	                    n++;
	                    
	                    msg = new ACLMessage(ACLMessage.REFUSE);
	                	msg.setContent("Hi! Wanna date?");
	                	
	                	for (int i =  0; i < agents.length; i++) {
	                		//if (!agents[i].getName().getLocalName().contentEquals(name))
	                		msg.addReceiver(agents[i].getName());
	                	}
	                	
	                	send(msg);
	                	
                	} else {
                		msg = receive();
                		            		
                		if(msg != null) {
                			System.out.println("Im " + getLocalName() + " and I recieve: " + msg.getContent() + " from " + msg.getSender());
                			if(!name.equals("JESUS2")) {
                			
                				ACLMessage reply = msg.createReply();
                				reply.setPerformative(ACLMessage.INFORM);
                				reply.setContent("YESSSSSSS!!!!");
                				send(reply);
                			}
                		}
                		block();
                	}
	                	
                }
        
                public boolean done() {  
                	return n>=2;  
                }
            }
        );
    }  
} 