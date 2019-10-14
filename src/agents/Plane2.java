package agents;
import jade.core.Agent;
import jade.core.behaviours.*;

@SuppressWarnings("serial")
public class Plane2 extends Agent 
{       
	int flightHours;
	int fuelLeft;
	
	protected void setup() 
    {
    	Object[] args = getArguments();
    	
     	String s = (String) args[0];
     	
     	String[] split = s.split(" ");

    	flightHours = Integer.parseInt(split[0]);
    	fuelLeft = Integer.parseInt(split[1]);
    	
        addBehaviour( 

            new SimpleBehaviour( this ) 
            {
                int n=0;
                
                public void action() 
                {	
                    System.out.println( "Hello World! My name is " + getLocalName() + " I have " + fuelLeft +" fuel left and can fligh more " +  flightHours);
                    n++;
                }
        
                public boolean done() {  
                	return n>=3;  
                }
            }
        );
    }  
} 