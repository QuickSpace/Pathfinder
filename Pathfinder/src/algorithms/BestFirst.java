package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import entities.Entity;
import entities.Node;
import entities.Obstacle;
import entities.SideNode;
import math.Point;
import views.AnimatePath;

import java.util.*;

public class BestFirst {
	
	private Map<Node, Node> pathMap;
	private Node startNode, endNode;
	Entity player;
	Entity target;
	ArrayList<Obstacle> obstacles;
	int iterations;
	boolean isRunning = false;
	private boolean neighbours = false;
	boolean dtype = false;
	Map<Node, Integer> closedList;
	public static final int MAX_ITERATIONS = 0x4000;
	
	Point selectedNode = new Point();
	
	AnimatePath view;
	
	public int gridSize, xOffSet, x0, dd;

	public BestFirst(Entity target, Entity player, ArrayList<Obstacle> obstacles, AnimatePath view, int dd, boolean neighbours) {
		this.target = target;
		this.player = player;
		this.dd = dd;
		this.neighbours = neighbours;
		this.obstacles = obstacles;
		selectedNode = player.getPosition();
		this.view = view;
	}
	
	public ArrayList<Point> startBestFirst() {
		startNode = new Node(player.getGx(), player.getGy());
		endNode = new Node(target.getGx(), target.getGy());
		ArrayList<Point> savedPoints = new ArrayList<Point>();
		List<Pair<Node, Integer>> openList = new LinkedList<Pair<Node, Integer>>();
		closedList = new HashMap<Node, Integer>();
		pathMap = new HashMap<Node, Node>();
		
		Node currentNode;
		
		openList.add(new Pair<Node, Integer>(startNode, manhattan(startNode)));
		
		long startTime = System.currentTimeMillis();
		
		while(!openList.isEmpty()) {
			NodeCompare comparator = new NodeCompare();
			Collections.sort(openList, comparator);
			currentNode = openList.get(0).getA();
			closedList.put(currentNode, openList.get(0).getB());
			openList.remove(0);
			
			iterations++;
			if(currentNode.getX() == endNode.getX() && currentNode.getY() == endNode.getY())
				pathMap.put(endNode, currentNode);
			
			ArrayList<Node> neighboursList = getNeighbours(currentNode);
			for (Node node : neighboursList) {
				if(neighbours)
					view.drawSideNode(new SideNode(node.getX(), node.getY()));
                if (!closedList.containsKey(node)) {
                    if (!openList.contains(new Pair<Node, Integer>(node, 0))) {
                        openList.add(new Pair<Node, Integer>(node, manhattan(node)));
                        pathMap.put(node, currentNode);
                    }
                } else {
                    if (manhattan(currentNode) + 10 < closedList.get(node)) {
                        pathMap.remove(node);
                        pathMap.put(node, currentNode);
                    }
                }
            }
			
			if (pathMap.containsKey(endNode)) {
                break;
            }
		}
		
		long lastTime = System.currentTimeMillis();
		int diff = (int) (lastTime - startTime);
		view.setInfo(iterations, diff);
		savedPoints = showPath();
		
		return savedPoints;
	}
	
	class NodeCompare implements Comparator<Pair> {
		
		public NodeCompare() {
			
		}

		@Override
		public int compare(Pair a, Pair b) {
			return ((Integer) a.getB()).compareTo((Integer) b.getB());
		}
		
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
				
				Node newNode = new Node(checkX, checkY);
				boolean flag = closedList.containsKey(newNode);
				
				if(!isWalkable(checkX, checkY) || flag)
					continue;
					
				// Позиции в экранных координатах (временные переменные)
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
	
	protected int heuristic(Node node) {
        return Math.abs(node.getX() - target.getGx()) + Math.abs(node.getY() - target.getGy());
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
	
	public static class Pair<A, B> {
		private A a;
        private B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }

        public B getB() {
            return b;
        }

        public void setB(B b) {
            this.b = b;
        }

        @Override
        public int hashCode() {
            return a.hashCode() * b.hashCode();
        }

        @Override
        public String toString() {
            return "< " + a.toString() + " , " + b.hashCode() + " >";
        }

        @SuppressWarnings("rawtypes")
		@Override
        public boolean equals(Object obj) {
            if (obj instanceof Pair) {
                Pair p = (Pair) obj;
                return p.a.equals(a);
            }
            return false;
        }
	}

}
