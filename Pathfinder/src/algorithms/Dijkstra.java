package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import algorithms.BestFirst.Pair;
import entities.Entity;
import entities.Node;
import entities.Obstacle;
import math.Point;
import views.AnimatePath;

public class Dijkstra {
	
	private Map<Node, Node> pathMap;
	private Node startNode, endNode;
	Entity player;
	Entity target;
	ArrayList<Obstacle> obstacles;
	int iterations;
	boolean isRunning = false;
	Map<Node, Integer> closedList;
	public static final int MAX_ITERATIONS = 0x4000;
	
	AnimatePath view;
	
	public int gridSize, xOffSet, x0, dd;

	public Dijkstra(Entity target, Entity player, ArrayList<Obstacle> obstacles, AnimatePath view, int dd) {
		this.target = target;
		this.player = player;
		this.dd = dd;
		this.obstacles = obstacles;
		this.view = view;
	}
	
	public ArrayList<Point> startDijkstra() {
		startNode = new Node(player.getGx(), player.getGy());
		endNode = new Node(target.getGx(), target.getGy());
		Set<Node> closedSet = new HashSet<Node>();
		Map<Node, Integer> unsettledNodes = new HashMap<Node, Integer>();
		ArrayList<Point> savedPoints = new ArrayList<Point>();
		pathMap = new HashMap<Node, Node>();
		LinkedList<Pair<Node, Integer>> mainQueue = new LinkedList<Pair<Node, Integer>>();
		
		Node currentNode = startNode;
		
		unsettledNodes.put(currentNode, 0);
		
		long startTime = System.currentTimeMillis();
		
		do {
			closedSet.add(currentNode);
			mainQueue.pollFirst();
			
			iterations++;
			ArrayList<Node> neighboursList = getNeighbours(currentNode);
			for(Node node : neighboursList) {
				if(!closedSet.contains(node)) {
					if (!mainQueue.contains(new Pair<Node, Integer>(node, 0))) {
						mainQueue.addLast(new Pair<Node, Integer>(node, unsettledNodes.get(currentNode) + 1));
					}
					if(unsettledNodes.containsKey(node)) {
						pathMap.put(node, Math.min(unsettledNodes.get(node),
						mainQueue.getLast().getB()) == mainQueue.getLast().getB() ? currentNode : pathMap.get(node));
						unsettledNodes.put(node, Math.min(unsettledNodes.get(node), mainQueue.getLast().getB()));
					} else {
						unsettledNodes.put(node, mainQueue.getLast().getB());
						pathMap.put(node, currentNode);
					}
				}
				
			}
			NodeComparator comparator = new NodeComparator();
			Collections.sort(mainQueue, comparator);
			if(!mainQueue.isEmpty())
				currentNode = mainQueue.peekFirst().getA();
			else
				currentNode = null;
            if (currentNode == null || pathMap.containsKey(endNode)) {
                break;
            }
		} while (!mainQueue.isEmpty());
		
		long lastTime = System.currentTimeMillis();
		int diff = (int) (lastTime - startTime);
		view.setInfo(iterations, diff);
		savedPoints = showPath();
		
		return savedPoints;
	}
	
	class NodeComparator implements Comparator<Pair> {
		
		public NodeComparator() {
			
		}

		@Override
		public int compare(Pair a, Pair b) {
			return clamp(((Integer) a.getB()).compareTo((Integer) b.getB()) & ((Integer) a.getA().hashCode()).compareTo(b.getA().hashCode()), -1, 1);
		}
		
	}
	
	public int clamp(int value, int min, int max) {
		if(value >= max)
			return max;
		else if(value <= min)
			return min;
		return value;
	}
	
	public ArrayList<Point> showPath() {
		ArrayList<Point> positions = new ArrayList<Point>();
		List<Node> path = new ArrayList<Node>();
        if (pathMap.containsKey(endNode)) {
            path = buildPath();
            Collections.reverse(path);
        }
        
        for(Node node : path) {
        	positions.add(new Point(node.getX(), node.getY()));
        }
        
        return positions;
    }

    public List<Node> buildPath() {
        List<Node> path = new ArrayList<Node>();
        Node Node = endNode;
        while (!Node.equals(startNode)) {
            Node = pathMap.get(Node);
            path.add(Node);
        }
        path.remove(path.size() - 1);
        return path;
    }
    
    public int min(int a, int b) {
    	return a < b ? a : b;
    }
    
    public ArrayList<Node> getNeighbours(Node node) {
		ArrayList<Node> neighbours = new ArrayList<Node>();
		for(int x = -1; x <= 1; x++) {
			for(int y = -1; y <= 1; y++) {
				if(x == 0 && y == 0)
					continue;
				if((x == -1 || x == 1) && (y == -1 || y == 1))
					continue;
				
				int checkX = node.getX() + x;
				int checkY = node.getY() + y;
				
				if(checkX > view.getGridSize() - 1 || checkY > view.getGridSize() - 1 || checkX < 0 || checkY < 0)
					continue;
				
				if(!isWalkable(checkX, checkY))
					continue;
					
				// Positions in screen coords (temporary)
				int t1 = (int) view.convertToScreenCoords(checkX, checkY, true).getX();
				int t2 = (int) view.convertToScreenCoords(checkX, checkY, true).getY();
				
				if(t1 >= x0 && t1 < view.getCanvasWidth() - view.getXoffset() && t2 <= view.getCanvasHeight() - view.getYoffset()) {
					neighbours.add(new Node(checkX, checkY));
				}
			}
		}
		
		return neighbours;
	}
	
	/**
	 * 
	 * Передавать в координатах сетки
	 * 
	 * @param x
	 * @param y
	 * @param лист препятствий
	 * @return наличие препятствия на данной координате
	 */
	
	public boolean isWalkable(int x, int y) {
		for(Obstacle o : obstacles) {
			if(o.getGx() == x && o.getGy() == y) 
				return false;
		}
		
		return true;
	}
	
	public int manhattan(Node node) {
		int distanceX = Math.abs(node.getX() - target.getGx());
		int distanceY = Math.abs(node.getY() - target.getGy());
		
		int d = Node.DIAGONAL_COST;
		int h = Node.CELL_COST;
		
		if(distanceX > distanceY) 
			return d * distanceY + h * (distanceX - distanceY);
		else 
			return d * distanceX + h * (distanceY - distanceX);
	}

}
