package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

public class Util 
{       
	private static int movementCost = 200;
	static int fuelConsumptionByMove = 1;
	private static HashMap<String, String> conflicts = new HashMap<String, String>();
	private static String initiator = "null";
	private static String responder = "null";
	public static boolean conflict = false;
	public static int confirmedConflictCounter = 0;
	public static int nResponders;

	String genCID(String cidBase, int cidCnt, String name) {
		if(cidBase == null) {
			cidBase = name + hashCode() + System.currentTimeMillis()%10000 + "_";
		}
		return cidBase + (cidCnt++);
	}
	 
	public static void move(Queue<String> route, HashMap<String, Integer> actualPos, int distanceLeft) {
		String nextMove = route.remove();
		distanceLeft--;
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
	
	public static String[][] refactorTrafficArray(String trafficS) {
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
        return tempResult.toArray(new String[tempResult.size()][]);        
	}
	
	public static void printTraffic(String[][] traffic) {
		System.out.println();
		for (int i = 0; i < traffic.length; i++) {
			for (int j = 0; j < traffic[i].length; j++) {
				System.out.print(traffic[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static boolean checkConflict(String[] actualPos, String[][] traffic, String name) {
		int actX = Integer.parseInt(actualPos[0]);
		int actY = Integer.parseInt(actualPos[1]);
		int size = traffic.length;
		
		//check DDR
		if(actX + 1 < size && actY + 1 < size) {
			if(!traffic[actX + 1][actY + 1].equals("null")) {
				conflicts.put(name, traffic[actX + 1][actY + 1]);
				conflicts.put(traffic[actX + 1][actY + 1], name);
				return true;
			}
		}
	
		
		//check DDL
		if(actX + 1 < size && actY - 1 >= 0) {
			if(!traffic[actX + 1][actY - 1].equals("null")) {
				conflicts.put(name, traffic[actX + 1][actY - 1]);
				conflicts.put(traffic[actX + 1][actY - 1], name);
				return true;
			}
		}
		
		//check DUL
		if(actX - 1 >= 0 && actY - 1 >= 0) {
			if(!traffic[actX - 1][actY - 1].equals("null")) {
				conflicts.put(name, traffic[actX - 1][actY - 1]);
				conflicts.put(traffic[actX - 1][actY - 1], name);
				return true;
			}
		}
		
		//check DUR
		if(actX - 1 >= 0 && actY + 1 < size) {
			if(!traffic[actX - 1][actY + 1].equals("null")) {
				conflicts.put(name, traffic[actX - 1][actY + 1]);
				conflicts.put(traffic[actX - 1][actY + 1], name);
				return true;
			}
		}
		
		//check U
		if(actX - 1 >= 0) {
			if(!traffic[actX - 1][actY].equals("null")) {
				conflicts.put(name, traffic[actX - 1][actY]);
				conflicts.put(traffic[actX - 1][actY], name);
				return true;
			}
		}
		
		//check D
		if(actX + 1 < size) {
			if(!traffic[actX + 1][actY].equals("null")) {
				conflicts.put(name, traffic[actX + 1][actY]);
				conflicts.put(traffic[actX + 1][actY], name);
				return true;
			}
		}
		
		//check R
		if(actY + 1 < size) {
			if(!traffic[actX][actY + 1].equals("null")) {
				conflicts.put(name, traffic[actX][actY + 1]);
				conflicts.put(traffic[actX][actY + 1], name);
				return true;
			}
		}
		
		//check L
		if(actY - 1 >= 0) {
			if(!traffic[actX][actY - 1].equals("null")) {
				conflicts.put(name, traffic[actX][actY - 1]);
				conflicts.put(traffic[actX][actY - 1], name);
				return true;
			}
		}
		
		return false;
	}

	public static HashMap<String, String> getConflicts() {
		return conflicts;
	}

	public static void setConflicts(HashMap<String, String> conflicts) {
		Util.conflicts = conflicts;
	}

	public static int getMovementCost() {
		return movementCost;
	}

	public static void setMovementCost(int movementCost) {
		Util.movementCost = movementCost;
	}

	public static String getInitiator() {
		return initiator;
	}

	public static void setInitiator(String initiator) {
		Util.initiator = initiator;
	}

	public static String getResponder() {
		return responder;
	}

	public static void setResponder(String responder) {
		Util.responder = responder;
	}
} 