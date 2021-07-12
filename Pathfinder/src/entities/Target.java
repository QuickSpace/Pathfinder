package entities;

import math.Vector;

public class Target {
	
	private int x, y;
	
	public Target(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean isOverlap(Player player) {
		if(player.getX() == this.getX() && player.getY() == this.getY())
			return true;
		
		return false;
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector getPosition() {
		return new Vector(x, y);
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
	
}
