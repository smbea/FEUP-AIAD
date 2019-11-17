package utils;

public class Node {
	// (x, y) represents coordinates of a cell in matrix
	int x, y;

	// maintain a parent node for printing final path
	Node parent;

	Node(int x, int y, Node parent) {
		this.x = x;
		this.y = y;
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ')';
	}
}
