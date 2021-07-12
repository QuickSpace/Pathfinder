package entities;

import math.Point;
import views.AnimatePath;

public class Entity {
	
	private int gx, gy;
	private int x, y;
	private boolean isDeleted;
	AnimatePath view;
	
	public Entity(int x, int y, AnimatePath view) {
		this.x = x;
		this.y = y;
		this.view = view;
		Point op = view.convertToGridCoords(x, y);
		this.gx = (int) op.getX();
		this.gy = (int) op.getY();
	}
	
	/**
	 * Для рендера!
	 * 
	 * @param entity начальная/конечная точка
	 * @return
	 */
	
	public boolean isOverlap(Entity entity) {
		return (entity.getGx() == gx && entity.getGy() == gy);
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Point getPosition() {
		return new Point(x, y);
	}

	public int getX() {
		return x;
	}
	
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	public boolean getDeleted() {
		return isDeleted;
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
	
	public Point getGPosition() {
		return new Point(gx, gy);
	}

	public int getGx() {
		return gx;
	}

	public void setGx(int gx) {
		this.gx = gx;
		this.x = (int) view.convertToScreenCoords(gx, gy, true).getX();
		this.y = (int) view.convertToScreenCoords(gx, gy, true).getY();
	}

	public int getGy() {
		return gy;
	}

	public void setGy(int gy) {
		this.gy = gy;
		this.x = (int) view.convertToScreenCoords(gx, gy, true).getX();
		this.y = (int) view.convertToScreenCoords(gx, gy, true).getY();
	}
	
}