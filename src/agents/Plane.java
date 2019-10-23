package agents;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class Plane extends Agent 
{       
	int predictedHours;
	int fuelLeft;
	int speed;
	float timeLeft;
	int money;
	int movementCost = 200;
	String name;
	boolean finished = false;
	boolean comm = false;
	
	Queue<String> route = new LinkedList<>(); 
	
	HashMap<String, Integer> actualPos = new HashMap<String, Integer>();
	HashMap<String, Integer> finalPos = new HashMap<String, Integer>();
	
	String[][] traffic = new String[5][5];
	
	AMSAgentDescription [] agents = null;
	AID myID;
	ACLMessage msg = null;
	
	String goal;								//money, time, fuel, etc
	String type;                                //competitive, cooperative 
	
	/**
	 * Parsing arguments and create route hardcoded for each agent (need to change)
	 */
	protected void argParsing() {
		Object[] args = getArguments();
     	String s = (String) args[0];
     	String[] splitInfo = s.split(" ");

    	actualPos.put("x", Integer.parseInt(splitInfo[0]));
    	actualPos.put("y", Integer.parseInt(splitInfo[1]));
    	
    	finalPos.put("x", Integer.parseInt(splitInfo[2]));
    	finalPos.put("y", Integer.parseInt(splitInfo[3]));
    	
    	fuelLeft = Integer.parseInt(splitInfo[4]);
    	speed = Integer.parseInt(splitInfo[5]);
    	timeLeft = Integer.parseInt(splitInfo[6]);
    	money = Integer.parseInt(splitInfo[7]);
    	goal = (String) splitInfo[8];
    	type = (String) splitInfo[9];
    	name = getLocalName();
    	
    	//hardcoded need to change in the future
    	
    	if(name.equals("Jesus2")) {
    		route.add("DDR");
    		route.add("DDR");
    		route.add("DDR");
    		route.add("DDR");
    	} else {
    		route.add("DUL");
    		route.add("DUL");
    		route.add("DUL");
    	}
	}
	
	protected void move() {
		String nextMove = route.remove();
		switch (nextMove) {
		case "DDR":
			actualPos.replace("x", actualPos.get("x") + 1);
			actualPos.replace("y", actualPos.get("y") + 1);
			break;
		case "DDL":
			actualPos.replace("x", actualPos.get("x") + 1);
			actualPos.replace("y", actualPos.get("y") - 1);
			break;
		case "DUL":
			actualPos.replace("x", actualPos.get("x") - 1);
			actualPos.replace("y", actualPos.get("y") - 1);
			break;		
		case "DUR":
			actualPos.replace("x", actualPos.get("x") - 1);
			actualPos.replace("y", actualPos.get("y") + 1);
			break;
		case "U":
			actualPos.replace("x", actualPos.get("x") - 1);
			break;
		case "D":
			actualPos.replace("x", actualPos.get("x") + 1);
			break;
		case "R":
			actualPos.replace("y", actualPos.get("y") + 1);
			break;
		case "L":
			actualPos.replace("y", actualPos.get("y") - 1);
			break;
		default:
			break;
		}
	}
	
	protected void refactorTrafficArray(String trafficS) {
		String[] strings = trafficS.replace("[", "").replace("]", ">").split(", ");
        List<String> stringList = new ArrayList<>();
        List<String[]> tempResult = new ArrayList<>();
        for(String str : strings) {
            if(str.endsWith(">")) {
               str = str.substring(0, str.length() - 1);
               if(str.endsWith(">")) {
                   str = str.substring(0, str.length() - 1);
               }
               stringList.add(str);
               tempResult.add(stringList.toArray(new String[stringList.size()]));
                stringList = new ArrayList<>();
            } else {
                stringList.add(str);
            }
        }
        traffic = tempResult.toArray(new String[tempResult.size()][]);
	}
	
	protected void printTraffic() {
		for (int i = 0; i < traffic.length; i++) {
			for (int j = 0; j < traffic[i].length; j++) {
				System.out.print(traffic[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	protected String checkConflict() {

		int actX = actualPos.get("x");
		int actY = actualPos.get("y");
		int size = traffic.length;
		
		//check DDR
		if(actX + 1 < size && actY + 1 < size) {
			if(!traffic[actX + 1][actY + 1].equals("null")) {
				return traffic[actX + 1][actY + 1];
			}
		}
		
		//check DDL
		if(actX + 1 < size && actY - 1 >= 0) {
			if(!traffic[actX + 1][actY - 1].equals("null")) {
				return traffic[actX + 1][actY + 1];
			}
		}
		
		//check DUL
		if(actX - 1 >= 0 && actY - 1 >= 0) {
			if(!traffic[actX - 1][actY - 1].equals("null")) {
				return traffic[actX + 1][actY + 1];
			}
		}
		
		//check DUR
		if(actX - 1 >= 0 && actY + 1 < size) {
			if(!traffic[actX + 1][actY - 1].equals("null")) {
				return traffic[actX + 1][actY + 1];
			}
		}
		
		//check U
		if(actX - 1 >= 0) {
			if(!traffic[actX - 1][actY].equals("null")) {
				return traffic[actX + 1][actY + 1];
			}
		}
		
		//check D
		if(actX + 1 < size) {
			if(!traffic[actX + 1][actY].equals("null")) {
				return traffic[actX + 1][actY + 1];
			}
		}
		
		//check R
		if(actY + 1 < size) {
			if(!traffic[actX][actY + 1].equals("null")) {
				return traffic[actX + 1][actY + 1];
			}
		}
		
		//check L
		if(actY - 1 >= 0) {
			if(!traffic[actX][actY - 1].equals("null")) {
				return traffic[actX + 1][actY + 1];
			}
		}
		
		return "none";
	}
	
	protected void setup() 
    {
		argParsing();
		
		Behaviour movement = new TickerBehaviour(this, (movementCost/speed)*1000) {
			
			@Override
			protected void onTick() {
				if(actualPos.get("x") == finalPos.get("x") && actualPos.get("y") == finalPos.get("y")) {
					System.out.println("I " + name + " arrived at destiny");
					finished = true;
					stop();
				}
				
				if(name.equals("Jesus2") && !finished) {
					move();
					comm = false;
					addBehaviour(new SimpleBehaviour() {
						
						@Override
						public boolean done() {
							return comm;
						}
						
						@Override
						public void action() {
							ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.setContent("traffic " + actualPos.get("x") + " " + actualPos.get("y"));
							msg.addReceiver(getAID("control"));
							send(msg);
							
							ACLMessage answer = new ACLMessage(ACLMessage.INFORM);
							answer = blockingReceive();
							String s = answer.getContent();
							refactorTrafficArray(s);
							String conflictPlane = checkConflict();
							if(!conflictPlane.equals("none")){
								System.out.println("Conflicted detected!! Starting negotiations with " + conflictPlane);
								stop();
							}
							comm = true;
						}
					});
					System.out.println("I " + name + " moved");
				}
			}
		};
		
		addBehaviour(movement);
    }  
} 