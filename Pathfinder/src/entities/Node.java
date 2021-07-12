package entities;

import java.util.ArrayList;

import math.Point;
import views.AnimatePath;

public class Node {
	
	public static int CELL_COST = 10;
	public static int DIAGONAL_COST = 14;
	private int bCost;
	private int gCost, hCost;
	Integer heapIndex;
	Integer x, y;
	Integer fullCost;
	boolean isClosed;
	AnimatePath view;
	private Node parent;
	
	public Node(Point position) {
		this.x = (int) position.getX();
		this.y = (int) position.getY();
	}
	
	public Node(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Node() {
		
	}
	
	public boolean isWalkable(ArrayList<Obstacle> obstacles) {
		for(Obstacle o : obstacles) {
			if(o.getX() == x && o.getY() == y) return false;
		}
		
		return true;
	}
	
	public boolean isWalkable(Obstacle o) {
		if(o.getGx() == x && o.getGy() == y)
			return false;
		
		return true;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public int fCost() {
		fullCost = gCost + hCost;
		return (int) fullCost;
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
    public int hashCode() {
        return x.hashCode() + y.hashCode();
    }
	
	@Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            Node node = (Node) obj;
            return node.x.equals(x) && node.y.equals(y);
        }
        return false;
    }
	
	public boolean equalNode(Object obj) {
		if(obj instanceof Node) {
			Node node = (Node) obj;
			return (node.getX() == x && node.getY() == y);
		}
		return false;
	}
	
	public int compareNodes(Node node) {
		int compare = node.getFullCost().compareTo(getFullCost());
		if(compare == 0) {
			if(node.gethCost() > this.gethCost()) {
				compare = 1;
			}
			else if(node.gethCost() == this.gethCost()) {
				compare = 0;
			}
			else {
				compare = -1;
			}
		}
		
		return compare;
	}

	public int getHeapIndex() {
		return heapIndex;
	}

	
	public void setHeapIndex(int heapIndex) {
		this.heapIndex = heapIndex;
	}
	
	public Point getPosition() {
		return new Point(x, y);
	}
	
	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getgCost() {
		return gCost;
	}

	public int getbCost() {
		return bCost;
	}

	public void setbCost(int bCost) {
		this.bCost = bCost;
	}

	public void setgCost(int gCost) {
		this.gCost = gCost;
	}

	public int gethCost() {
		return hCost;
	}

	public void sethCost(int hCost) {
		this.hCost = hCost;
	}

	public Integer getFullCost() {
		return gCost + hCost;
	}

	public void setFullCost(Integer fullCost) {
		this.fullCost = fullCost;
	}

}
