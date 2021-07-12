package entities;

import views.AnimatePath;

public class Obstacle extends Entity {

	private float distance;
	private Obstacle neighbour;
	private boolean isClosed;
	
	public Obstacle(int x, int y, AnimatePath view) {
		super(x, y, view);
		this.isClosed = false;
	}

	public void setNeighbour(Obstacle n) {
		this.neighbour = n;
	}
	
	public Obstacle getNeighbour() {
		return neighbour;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
	
	public void setDistance(float d) {
		this.distance = d;
	}

	public float getDistance() {
		return distance;
	}
	
}
