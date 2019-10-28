package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

@SuppressWarnings("serial")
public class Util 
{       
	int movementCost = 200;
	int fuelConsumptionByMove = 1;
	
	String genCID(String cidBase, int cidCnt, String name) {
		if(cidBase == null) {
			cidBase = name + hashCode() + System.currentTimeMillis()%10000 + "_";
		}
		return cidBase + (cidCnt++);
	}
	 
	void move(Queue<String> route, HashMap<String, Integer> actualPos) {
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
	
	String[][] refactorTrafficArray(String trafficS) {
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
	
	void printTraffic(String[][] traffic) {
		System.out.println();
		for (int i = 0; i < traffic.length; i++) {
			for (int j = 0; j < traffic[i].length; j++) {
				System.out.print(traffic[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	String checkConflict(HashMap<String, Integer> actualPos, String[][] traffic) {
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
} 