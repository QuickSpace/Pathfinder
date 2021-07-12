package math;

import entities.Node;
import views.AnimatePath;

public class Vector {
	
	// Класс для векторной математики? Да, я больной на голову.
	
	private float x, y, length;
	AnimatePath view;

	public Vector(float x, float y) {
		this.x = x;
		this.y = y;
		this.length = length(x, y);
	}
	
	public Vector(AnimatePath view) {
		this.view = view;
	}
	
	public Vector() {
		
	}
	
	public Vector toGridCoords() {
		int coords[] = new int[2];
		
		coords[0] = ((int) x - view.getXoffset()) / view.getDd();
		coords[1] = (int) y / view.getDd();
		
		return new Vector(coords[0], coords[1]);
	}
	
	public float length(float x, float y) {
		return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	
	public float calculateDistance(Vector v1, Vector v2) {
		float dx = (v1.getX() - v2.getX());
		float dy = (v1.getY() - v2.getY());
		
		return (float) Math.sqrt((Math.pow(dx, 2) + Math.pow(dy, 2)));
	}
	
	public float calculateDistance(int[] startCoords, int[] endCoords) {
		float dx = (startCoords[0] - endCoords[0]);
		float dy = (startCoords[1] - endCoords[1]);
		
		return (float) Math.sqrt((Math.pow(dx, 2) + Math.pow(dy, 2)));
	}
	
	public int calculateDistance(int x1, int y1, int x2, int y2) {
		float dx = Math.abs((x1 - x2));
		float dy = Math.abs((y1 - y2));
		
		return (int) Math.sqrt((Math.pow(dx, 2) + Math.pow(dy, 2))) * Node.CELL_COST;
	}
	
	public Vector subtract(Vector v1, Vector v2) {
		float x1, x2, y1, y2;
		
		x1 = v1.getX();
		x2 = v2.getX();
		y1 = v1.getY();
		y2 = v2.getY();
		
		Vector v = new Vector();
		v.setX(x2 - x1);
		v.setY(y1 - y2);
		
		return v;
	}
	
	public Vector sum(Vector v1, Vector v2) {
		float x1, x2, y1, y2;
		
		x1 = v1.getX();
		x2 = v2.getX();
		y1 = v1.getY();
		y2 = v2.getY();
		
		Vector v = new Vector();
		v.setX(x2 + x1);
		v.setY(y1 + y2);
		
		return v;
	}
	
	public float dot(Vector v1, Vector v2) {
		return v1.getX() * v2.getX() + v1.getY() * v2.getY();
	}
	
	public float dotAngle(Vector v1, Vector v2, float angle) {
		return (float) (v1.getLength() * v2.getLength() * Math.cos(angle));
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

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

}
