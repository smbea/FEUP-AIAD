package utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class Util {
	private static int movementCost = 200;
	/**
	 * Plane average total fuel loss of 10 L/km (liters per kilometer)
	 */
	public static int fuelLoss = 10;
	private static HashMap<String, String> conflicts = new HashMap<String, String>();
	private static String initiator = "null";
	private static String responder = "null";
	public static boolean conflict = false;
	public static int confirmedConflictCounter = 0;
	public static int nResponders;
	public static HashMap<String, Stack<HashMap<String, Integer>>> routes = new HashMap<String, Stack<HashMap<String, Integer>>>();
	public static int mapSize;
	
	// Below arrays details all 8 possible movements from a cell
	private static int[] row = { -1, 0, 0, 1, -1, -1, 1, 1};
	private static int[] col = { 0, -1, 1, 0, -1, 1, -1, 1};

	String genCID(String cidBase, int cidCnt, String name) {
		if (cidBase == null) {
			cidBase = name + hashCode() + System.currentTimeMillis() % 10000 + "_";
		}
		return cidBase + (cidCnt++);
	}

	public static void move(String agentName, HashMap<String, Integer> actualPos, int distanceLeft) {
		Stack<HashMap<String, Integer>> route = routes.get(agentName);
		HashMap<String, Integer> nextMove = route.pop();
		
		if (nextMove.get("x") == actualPos.get("x") && nextMove.get("y") == actualPos.get("y")) {
			nextMove = route.pop();
		}
		distanceLeft--;
		
		actualPos.replace("x", nextMove.get("x"));
		actualPos.replace("y", nextMove.get("y"));
	}

	public static boolean checkConflict(String[] actualPos, String[][] traffic, String name) {
		int actX = Integer.parseInt(actualPos[0]);
		int actY = Integer.parseInt(actualPos[1]);
		int size = traffic.length;

		// check DDR
		if (actX + 1 < size && actY + 1 < size) {
			if (!traffic[actX + 1][actY + 1].equals("null")) {
				conflicts.put(name, traffic[actX + 1][actY + 1]);
				conflicts.put(traffic[actX + 1][actY + 1], name);
				return true;
			}
		}

		// check DDL
		if (actX + 1 < size && actY - 1 >= 0) {
			if (!traffic[actX + 1][actY - 1].equals("null")) {
				conflicts.put(name, traffic[actX + 1][actY - 1]);
				conflicts.put(traffic[actX + 1][actY - 1], name);
				return true;
			}
		}

		// check DUL
		if (actX - 1 >= 0 && actY - 1 >= 0) {
			if (!traffic[actX - 1][actY - 1].equals("null")) {
				conflicts.put(name, traffic[actX - 1][actY - 1]);
				conflicts.put(traffic[actX - 1][actY - 1], name);
				return true;
			}
		}

		// check DUR
		if (actX - 1 >= 0 && actY + 1 < size) {
			if (!traffic[actX - 1][actY + 1].equals("null")) {
				conflicts.put(name, traffic[actX - 1][actY + 1]);
				conflicts.put(traffic[actX - 1][actY + 1], name);
				return true;
			}
		}

		// check U
		if (actX - 1 >= 0) {
			if (!traffic[actX - 1][actY].equals("null")) {
				conflicts.put(name, traffic[actX - 1][actY]);
				conflicts.put(traffic[actX - 1][actY], name);
				return true;
			}
		}

		// check D
		if (actX + 1 < size) {
			if (!traffic[actX + 1][actY].equals("null")) {
				conflicts.put(name, traffic[actX + 1][actY]);
				conflicts.put(traffic[actX + 1][actY], name);
				return true;
			}
		}

		// check R
		if (actY + 1 < size) {
			if (!traffic[actX][actY + 1].equals("null")) {
				conflicts.put(name, traffic[actX][actY + 1]);
				conflicts.put(traffic[actX][actY + 1], name);
				return true;
			}
		}

		// check L
		if (actY - 1 >= 0) {
			if (!traffic[actX][actY - 1].equals("null")) {
				conflicts.put(name, traffic[actX][actY - 1]);
				conflicts.put(traffic[actX][actY - 1], name);
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	public static int printPath(Node node) {
		if (node == null) {
			return 0;
		}
		int len = printPath(node.parent);
		System.out.print(node + " ");
		return len + 1;
	}
	
	public static int saveRoute(Node node, Stack<HashMap<String, Integer>> coords) {
		HashMap<String, Integer> coord = new HashMap<String, Integer>();
		
		if(node == null) {
			return 0;
		}
		
		coord.put("x", node.x);
		coord.put("y", node.y);
		
		coords.push(coord);
		
		int len = saveRoute(node.parent, coords);

		return len + 1;
	}

	/**
	 * Determine whether position is valid. If not, return false.
	 * @param x
	 * @param y
	 * @param size
	 * @return
	 */
	private static boolean isValid(int x, int y, int size) {
		return (x >= 0 && x < size && y >= 0 && y < size);
	}

	/**
	 * Method to find shortest path to destination aplying BFS algorithm.
	 * Other planes are not considered as obstacles so that conflicts may occur.
	 * @param traffic
	 * @param x
	 * @param y
	 * @param destX
	 * @param destY
	 * @return
	 */
	public static Node findPath(int x, int y, int destX, int destY)
	{
		// create a queue and enqueue first node
		Queue<Node> q = new ArrayDeque<>();
		Node src = new Node(x, y, null);
		q.add(src);

		// set to check if matrix cell is visited before or not
		Set<String> visited = new HashSet<>();

		String key = src.x + "," + src.y;
		visited.add(key);

		// run till queue is not empty
		while (!q.isEmpty())
		{
			// pop front node from queue and process it
			Node curr = q.poll();
			int i = curr.x, j = curr.y;

			// return if destination is found
			if (i == destX && j == destY) {
				return curr;
			}

			// check all 4 possible movements from current cell
			// and recur for each valid movement
			for (int k = 0; k < 8; k++)
			{
				// get next position coordinates using value of current cell
				x = i + row[k];
				y = j + col[k];
				
				// check if it is possible to go to next position
				// from current position
				if (isValid(x, y, mapSize))
				{
					// construct next cell node
					Node next = new Node(x, y, curr);

					key = next.x + "," + next.y;

					// if it not visited yet
					if (!visited.contains(key)) {
						// push it into the queue and mark it as visited
						q.add(next);
						visited.add(key);
					}
				}
				
				x = i;
				y = j;
			}
		}

		// return null if path is not possible
		return null;
	}
	
	public static int createPossibleRoute(String proposal, HashMap<String, Integer> actualPos, int xf, int yf) {
		Pair<Integer, Integer> position = calculatePosition(proposal, actualPos);
		
		if (position != null) {
			int xi = position.getFirst();
			int yi = position.getSecond();
			
			Node node = Util.findPath(xi, yi, xf, yf);
	
			Stack<HashMap<String, Integer>> routeCoords = new Stack<>();
	
			return saveRoute(node, routeCoords);
		}
		
		return -1;
	}

	public static Pair<Integer, Integer> calculatePosition(String proposal, HashMap<String, Integer> actualPos) {
		int xi = actualPos.get("x");
		int yi = actualPos.get("y");

		System.out.println("recebe " + proposal);
		
		if(proposal.contains("Move down right")) {
			xi+=1;
			yi+=1;
		} else if (proposal.contains("Move down left")) {
			xi-=1;
			yi+=1;
		} else if (proposal.contains("Move top right")) {
			xi+=1;
			yi-=1;
		} else if (proposal.contains("Move top left")) {
			xi-=1;
			yi-=1;
		} else if (proposal.contains("Move up")) {
			yi-=1;
		} else if (proposal.contains("Move down")) {
			yi+=1;
		} else if (proposal.contains("Move right")) {
			xi+=1;
		} else if (proposal.contains("Move left")) {
			xi-=1;
		} else {
			return null;
		}
		
		if(!isValid(xi, yi, mapSize)) {
			return null;
		}

		return new Pair<Integer, Integer>(xi, yi);
	}
	
	public static String parseMove(String moveCode) {
		String move = null;
		
		switch(moveCode) {
		case "U":
			move = "up";
			break;
		case "D":
			move = "down";
			break;
		case "L":
			move = "left";
			break;
		case "R":
			move = "right";
			break;
		case "DDR":
			move = "down right";
			break;
		case "DDL":
			move = "down left";
			break;
		case "DUL":
			move = "top left";
			break;
		case "DUR":
			move = "top right";
			break;
		default:
			break;
		}
		
		return move;
	}
	
	public static String parseStringMove(String move) {
		String moveCode = null;
		
		switch(move) {
		case "up":
			moveCode = "U";
			break;
		case "down":
			moveCode = "D";
			break;
		case "left":
			moveCode = "L";
			break;
		case "right":
			moveCode = "R";
			break;
		case "down right":
			moveCode = "DDR";
			break;
		case "down left":
			moveCode = "DDL";
			break;
		case "top left":
			moveCode = "DUL";
			break;
		case "top right":
			moveCode = "DUR";
			break;
		default:
			break;
		}
		
		return moveCode;
	}
	
	public static HashMap<String, Integer> findAgentMap(String agent, String[][] traffic) {
		for (int i = 0; i < traffic.length; i++) {
			for (int j = 0; j < traffic.length; j++) {
				if (traffic[i][j].equals(agent)) {
					HashMap<String, Integer> coord = new HashMap<String, Integer>();
					
					coord.put("x", i);
					coord.put("y", j);
					
					return coord;
				}
			}
		}
		
		return null;
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