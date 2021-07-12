package entities;

import android.graphics.Color;
import math.Point;

public class SideNode {
	
	private int x, y;
	private Point position;
	private int closedCount;
	private int currColor;
	private int fullCost;
	public static final float RED_MULTIPLIER = 22.0f;
	public static final float BLUE_MULTIPLIER = 16.5f;
	
	public SideNode(int x, int y) {
		this.x = x;
		this.y = y;
		this.currColor = 10;
		position = new Point(x, y);
	}
	
	public SideNode(int x, int y, int color) {
		this.x = x;
		this.y = y;
		this.currColor = color;
		position = new Point(x, y);
	}
	
	public boolean positionExists(SideNode sn) {
		return (sn.getX() == x && sn.getY() == y);
	}

	public void closeSNode() {
		closedCount++;
		currColor = Color.argb(150, clamp((int) (currColor * RED_MULTIPLIER * closedCount), 0, 255), 127, 127);
	}
	
	public int clamp(int value, int minValue, int maxValue) {
		if(value >= maxValue)
			return maxValue;
		else if(value <= minValue)
			return minValue;
		
		return value;
	}
	
	public Point getPosition() {
		return position;
	}

	public int getFullCost() {
		return fullCost;
	}

	public void setFullCost(int fullCost) {
		this.fullCost = fullCost;
	}

	public void setPosition(Point position) {
		this.position = position;
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

	public int getClosedCount() {
		return closedCount;
	}

	public void setClosedCount(int closedCount) {
		this.closedCount = closedCount;
	}

	public int getCurrColor() {
		return currColor;
	}

	public void setCurrColor(int currColor) {
		this.currColor = currColor;
	}
	
}
