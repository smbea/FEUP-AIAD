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
	private static HashMap<String, String> conflicts = new HashMap<String, String>();
	public static boolean conflict = false;
	public static int confirmedConflictCounter = 0;
	public static int nResponders;
	public static int nActiveResponders;
	public static HashMap<String, Stack<HashMap<String, Integer>>> routes = new HashMap<String, Stack<HashMap<String, Integer>>>();
	public static int mapSize;
	public static int totalAcceptedProposals = 0;
	public static int totalProposals = 0;
	
	// Below arrays details all 8 possible movements from a cell
	private static int[] row = { -1, 0, 0, 1, -1, -1, 1, 1};
	private static int[] col = { 0, -1, 1, 0, -1, 1, -1, 1};

	String genCID(String cidBase, int cidCnt, String name) {
		if (cidBase == null) {
			cidBase = name + hashCode() + System.currentTimeMillis() % 10000 + "_";
		}
		return cidBase + (cidCnt++);
	}

	public static void move(String agentName, PlanePersonality plane) {
		Stack<HashMap<String, Integer>> route = routes.get(agentName);
		HashMap<String, Integer> nextMove = route.pop();
		
		if (nextMove.get("x") == plane.getActualPos().getFirst() && nextMove.get("y") == plane.getActualPos().getSecond()) {
			nextMove = route.pop();
		}

		plane.setActualPos(new Pair<>(nextMove.get("x"), nextMove.get("y")));
		plane.setDistanceLeft(plane.getDistanceLeft()-1);
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
	
	public static int createBestProposalRoute(String agentName, String proposal, Pair<Integer, Integer> actualPos, int xf, int yf) {
		Pair<Integer, Integer> position = calculatePosition(proposal, actualPos);
		
		if (position != null) {
			Stack<HashMap<String, Integer>> routeCoords = new Stack<>();
			int xi = position.getFirst();
			int yi = position.getSecond();
			
			Node node = Util.findPath(xi, yi, xf, yf);
			
			saveRoute(node, routeCoords);
			Util.routes.replace(agentName, routeCoords);
			
			System.out.println("Agent " + agentName + " is generating a new route ....");
			System.out.print("Shortest path is: ");
			Util.printPath(node);
	
			return 0;
		}
		
		return -1;
	}
	
	public static int createPossibleRoute(String proposal, Pair<Integer, Integer> actualPos, int xf, int yf) {
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

	public static Pair<Integer, Integer> calculatePosition(String proposal, Pair<Integer, Integer> actualPos) {
		int xi = actualPos.getFirst();
		int yi = actualPos.getSecond();

		if(proposal.contains("Move down right") || proposal.contains("MDR")) {
			xi+=1;
			yi+=1;
		} else if (proposal.contains("Move down left") || proposal.contains("MDL")) {
			xi-=1;
			yi+=1;
		} else if (proposal.contains("Move top right") || proposal.contains("MUR")) {
			xi+=1;
			yi-=1;
		} else if (proposal.contains("Move top left") || proposal.contains("MUL")) {
			xi-=1;
			yi-=1;
		} else if (proposal.contains("Move up") || proposal.contains("MU")) {
			yi-=1;
		} else if (proposal.contains("Move down") || proposal.contains("MD")) {
			yi+=1;
		} else if (proposal.contains("Move right") || proposal.contains("MR")) {
			xi+=1;
		} else if (proposal.contains("Move left") || proposal.contains("ML")) {
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
		case "MU":
			move = "up";
			break;
		case "MD":
			move = "down";
			break;
		case "ML":
			move = "left";
			break;
		case "MR":
			move = "right";
			break;
		case "MDR":
			move = "down right";
			break;
		case "MDL":
			move = "down left";
			break;
		case "MUL":
			move = "top left";
			break;
		case "MUR":
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
			moveCode = "MU";
			break;
		case "down":
			moveCode = "MD";
			break;
		case "left":
			moveCode = "ML";
			break;
		case "right":
			moveCode = "MR";
			break;
		case "down right":
			moveCode = "MDR";
			break;
		case "down left":
			moveCode = "MDL";
			break;
		case "top left":
			moveCode = "MUL";
			break;
		case "top right":
			moveCode = "MUR";
			break;
		default:
			break;
		}
		
		return moveCode;
	}
	
	public static Pair<Integer, Integer> findAgentMap(String agent, String[][] traffic) {
		for (int i = 0; i < traffic.length; i++) {
			for (int j = 0; j < traffic.length; j++) {
				if (traffic[i][j].equals(agent)) {
					return new Pair<>(i, j);
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
}