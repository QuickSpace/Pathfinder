package math;

import entities.Entity;

public class Point {
	
	private float x, y;

	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(Entity e) {
		x = e.getGx(); 
		y = e.getGy();
	}
	
	public Point() {
		
	}
	
	public Point subtract(Point v1, Point v2) {
		float x1, x2, y1, y2;
		
		x1 = v1.getX();
		x2 = v2.getX();
		y1 = v1.getY();
		y2 = v2.getY();
		
		return new Point(x2 - x1, y1 - y2);
	}
	
	public Point sum(Point v1, Point v2) {
		float x1, x2, y1, y2;
		
		x1 = v1.getX();
		x2 = v2.getX();
		y1 = v1.getY();
		y2 = v2.getY();
		
		return new Point(x2 + x1, y1 + y2);
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

}
