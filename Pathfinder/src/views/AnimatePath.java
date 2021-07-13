package views;

import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.widget.TextView;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;
import databases.Field;
import databases.SQLController;
import dialogs.GridSizeDialog;
import entities.Entity;
import entities.Node;
import entities.Obstacle;
import entities.SideNode;
import math.Point;
import pathfinder.MainActivity;
import algorithms.*;

public class AnimatePath extends View {
	
	// Canvas components/flags
	private Paint paint;
	private Paint strokePaint;
	private float length;
	SQLController controller;
	private int nX, nY, x0, y0, dd, gridSize, canvasWidth, canvasHeight;
	int xoffset, yoffset;
	private int iterations = 0;
	boolean diagonal = true;
	boolean enableNeighbours = false;
	private int animationSpeed;
	int id;
	private float angle;
	private int time = 0;
	private boolean classic = false;
	boolean drawPlayer, drawObstacle, drawTarget, drawPath, drawSideNode = false;
	
	// Path & animation
	private float[] dashes = { 1.0f, 0.0f };
	private ValueAnimator animator;
	private Path path;
	private Paint pathPaint = new Paint();
	private RectF rect = new RectF();
	
	Context context;
	TextView displayState;
	GridSizeDialog gsd = new GridSizeDialog();
	
	// Экземпляр класса для методов для алгоритма A*
	AStar astar;
	
	// Полотно
	Canvas canvas;
	
	Entity player;
	Obstacle obstacle;
	Entity target;
	Point vector;
	Node node;
	
	ArrayList<Point> checkpoints = new ArrayList<Point>();
	
	ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
	ArrayList<Point> loadObstaclePositions = new ArrayList<Point>();
	
	ArrayList<SideNode> sNodes = new ArrayList<SideNode>();
	
	private int playerColor = Color.GREEN, targetColor = Color.RED, pathColor = Color.BLUE;
	private String mapName;
	private boolean eDistance = false;

	public AnimatePath(Context context) {
		super(context);
		init(context);
	}
	
	public AnimatePath(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AnimatePath(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void init(Context context) {
		x0 = 20;
		y0 = 20;
		gridSize = 16;
		animationSpeed = 45;
		paint = new Paint();
		strokePaint = new Paint();
		path = new Path();
		this.context = context;
	}
	
	public void setPath(Path p, int duration) {
		path.reset();
		path.addPath(p);
		// Измерить путь
		PathMeasure measure = new PathMeasure(path, false);
		length = measure.getLength();
		// создать аниматор
		ObjectAnimator animator = ObjectAnimator.ofFloat(AnimatePath.this, "route", 1.0f, 0.0f);
		animator.setDuration(duration);
		animator.start(); // вызывает setRoute
	}
	
	public void setRoute(float route) {
		pathPaint.setPathEffect(createPathEffect(length, route, 0.0f));
		invalidate();
	}
	
	private static PathEffect createPathEffect(float pathLength, float route, float offset) {
		return new DashPathEffect(new float[] { pathLength, pathLength },
				Math.max(route * pathLength, offset));
	}
	
	// _id, mname, gsize, pcolor, tcolor, lcolor
	public void setMapParams(Field field) {
		id = field.id;
		mapName = field.mapName;
		gridSize = field.gsize;
		playerColor = field.pcolor;
		targetColor = field.tcolor;
		pathColor = field.lcolor;
		setGridSize(gridSize);
	}
	
	public void setGridSize(int gridValue, int id, String mapName, Point pPos, Point tPos, int pColor, int tColor, 
			int pathColor, ArrayList<Obstacle> obstacles) {
		this.gridSize = gridValue;
		this.id = id;
		this.mapName = mapName;
		this.playerColor = pColor;
		this.targetColor = tColor;
		this.pathColor = pathColor;
		Point playerScreenPos = convertToScreenCoords(pPos.getX(), pPos.getY(), true);
		Point targetScreenPos = convertToScreenCoords(tPos.getX(), tPos.getY(), true);
		player = new Entity((int) playerScreenPos.getX(), (int) playerScreenPos.getY(), this);
		target = new Entity((int) targetScreenPos.getX(), (int) targetScreenPos.getY(), this);
		this.obstacles = obstacles;
		invalidate();
	}
	
	public void startButton(int algorithm) {
		if(player == null || target == null) {
			Toast.makeText(getContext(), "Не удалось начать алгоритм!", Toast.LENGTH_SHORT).show();
			return;
		}
		if(player.getDeleted() || target.getDeleted()) {
			Toast.makeText(getContext(), "Необходимо поставить все конечные точки!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		sNodes.clear();
		MainActivity activity = (MainActivity) context;
		((TextView) activity.getDisplayState()).setText("Начало поиска!");
		switch(algorithm) {
			case 0:
				AStar(player.getPosition(), target.getPosition(), getDd(), animationSpeed);
				break;
			case 1:
				StartBestFirst(player.getPosition(), target.getPosition(), animationSpeed);
				break;
			case 2:
				StartDijkstra(player.getPosition(), target.getPosition(), animationSpeed);
		}
	}

	@Override
	protected void onDraw(Canvas c) {
		super.onDraw(c);
		
		canvasWidth = c.getWidth();
		canvasHeight = c.getHeight();

		// габариты Canvas
		nX = canvasWidth;
		nY = canvasHeight;
		dd = (nX > nY ? nY - y0 : nX - x0) / gridSize;
		
		// Размещение сетки по центру (установленное смещение не влияет на результат)
		x0 = (c.getWidth() - (gridSize * dd)) / 2;
		y0 = (c.getHeight() - (gridSize * dd)) / 2;
		
		xoffset = (canvasWidth - (gridSize * dd)) / 2;
		yoffset = (canvasHeight - (gridSize * dd)) / 2;
		
		if(!classic)
			paint.setColor(Color.WHITE);
		else
			paint.setColor(Color.BLACK);
		paint.setStrokeWidth(0);
		for (int i = 0; i < gridSize + 1; i++) {
			c.drawLine(x0 + dd * i, y0, x0 + dd * i, y0 + dd * gridSize, paint);
			c.drawLine(x0, y0 + dd * i, x0 + dd * gridSize, y0 + dd * i, paint);
		}
		
		// Прорисовка объектов (invalidate() для обновления)
		
		if(drawSideNode) {
			for(SideNode sn : sNodes) {
				paint.setColor(sn.getCurrColor());
				paint.setStrokeWidth(4.5f);
				paint.setStyle(Style.FILL_AND_STROKE);
				Point sp = convertToScreenCoords(sn.getX(), sn.getY(), true);
				int sx = (int) sp.getX();
				int sy = (int) sp.getY();
				rect.set(sx, sy, sx + dd, sy + dd);
				c.drawRoundRect(rect, 10.0f, 10.0f, paint);
			}
		}
		
		if(drawPlayer) {
			paint.setColor(playerColor);
			paint.setStrokeWidth(4.5f);
			paint.setStyle(Style.FILL_AND_STROKE);
			c.drawCircle(player.getX() + (dd / 2), player.getY() + (dd / 2), (float) (dd / Math.E), paint);
		}
		
		if(drawObstacle) {
			for(Obstacle o : obstacles) { 
				paint.setStyle(Paint.Style.FILL);
				strokePaint.setStyle(Paint.Style.STROKE);
				if(!classic) {
					paint.setColor(Color.rgb(170, 170, 170));
					strokePaint.setColor(Color.CYAN);
				} else {
					paint.setColor(Color.BLACK);
					strokePaint.setColor(Color.BLACK);
				}
				paint.setStrokeWidth(2.5f);
				rect.set(o.getX(), o.getY(), o.getX() + dd, o.getY() + dd);
				if(!classic) {
					c.drawRoundRect(rect, 10.0f, 10.0f, paint);
					c.drawRoundRect(rect, 10.0f, 10.0f, strokePaint);
				}
				else {
					c.drawRect(o.getX(), o.getY(), o.getX() + dd, o.getY() + dd, paint);
				}
			}
		}
		
		if(drawTarget) {
			paint.setColor(targetColor);
			paint.setStrokeWidth(4.5f);
			paint.setStyle(Style.FILL_AND_STROKE);
			c.drawCircle(target.getX() + (dd / 2), target.getY() + (dd / 2), (float) (dd / Math.E), paint);
		}
		
		if(drawPath) {
			pathPaint.setColor(pathColor);
			pathPaint.setStrokeWidth(5);
			pathPaint.setStyle(Paint.Style.STROKE);
			c.drawPath(path, pathPaint);
		}
	}
	
	public float angle() {
		if(player == null || target == null)
			return 0.0f;
		float dx = player.getX() - target.getX();
		float dy = player.getY() - target.getY();
		float d = (float) Math.sqrt(dx * dx + dy * dy);
		if(target.getX() <= player.getX())
			d = -d;
		float angle = (float) (Math.acos(dx / d) * 180f / Math.PI);
		return Math.round((dy >= 0 ? angle : 360.0f - angle) * 10) / 10;
	}

	public void AStar(Point startPoint, Point endPoint, int dd, int speed) {
		vector = new Point();
		Path path = new Path();
		astar = new AStar(target, player, obstacles, this, enableNeighbours, eDistance, dd);
		Point tempp = convertToGridCoords(player.getPosition().getX(), player.getPosition().getY());
		Point tempt = convertToGridCoords(target.getPosition().getX(), target.getPosition().getY());
		
		ArrayList<Point> positions = astar.findPath(new Point(tempp.getX(), tempp.getY()), new Point(tempt.getX(), tempt.getY()));
		
		if(positions == null || positions.isEmpty()) {
			MainActivity activity = (MainActivity) context;
			((TextView) activity.getDisplayState()).setText("Не удалось найти путь!");
			getPath().reset();
			invalidate();
			return;
		}
		
		float distance = calculateTotalDistance(positions);
		int totalTime = (int) distance / speed;
		
		if(enableNeighbours) {
			generateSideNodes(astar.getSideNodes());
		}
		
		path.moveTo(player.getX() + dd / 2, player.getY() + dd / 2);
		
		for(Point v : positions) {
			int x = (int) (convertToScreenCoords(v.getX(), v.getY(), true).getX() + dd / 2);
			int y = (int) (convertToScreenCoords(v.getX(), v.getY(), true).getY() + dd / 2);
				
			path.lineTo(x, y);
		}
		
		path.lineTo(target.getX() + dd / 2, target.getY() + dd / 2);
		drawPath = true;
		setPath(path, totalTime);
	}
	
	public void StartDijkstra(Point startPoint, Point endPoint, int speed) {
		Path path = new Path();
		Dijkstra dijkstra = new Dijkstra(target, player, obstacles, this, dd);
		
		ArrayList<Point> positions = dijkstra.startDijkstra();
		
		if(positions == null || positions.isEmpty()) {
			MainActivity activity = (MainActivity) context;
			((TextView) activity.getDisplayState()).setText("Не удалось найти путь!");
			getPath().reset();
			invalidate();
			return;
		}
		
		float distance = calculateTotalDistance(positions);
		int totalTime = (int) (distance / speed);
		
		if(enableNeighbours) {
			generateSideNodes(astar.getSideNodes());
		}
		
		path.moveTo(startPoint.getX() + dd / 2, startPoint.getY() + dd / 2);
		
		for(Point p : positions) {
			int x = (int) (convertToScreenCoords(p.getX(), p.getY(), true).getX() + dd / 2);
			int y = (int) (convertToScreenCoords(p.getX(), p.getY(), true).getY() + dd / 2);
			
			path.lineTo(x, y);
		}
		
		path.lineTo(endPoint.getX() + dd / 2, endPoint.getY() + dd / 2);
		drawPath = true;
		setPath(path, totalTime);
	}
	
	public void StartBestFirst(Point startPoint, Point endPoint, int speed) {
		Path path = new Path();
		BestFirst bf = new BestFirst(target, player, obstacles, this, dd, enableNeighbours);
		ArrayList<Point> positions = bf.startBestFirst();
		
		float distance = calculateTotalDistance(positions);
		int totalTime = (int) (distance / speed);
		
		if(positions == null || positions.isEmpty()) {
			MainActivity activity = (MainActivity) context;
			((TextView) activity.getDisplayState()).setText("Не удалось найти путь!");
			getPath().reset();
			invalidate();
			return;
		}
		
		if(enableNeighbours) {
			generateSideNodes(astar.getSideNodes());
		}
		
		path.moveTo(startPoint.getX() + dd / 2, startPoint.getY() + dd / 2);
		
		for(Point p : positions) {
			int x = (int) (convertToScreenCoords(p.getX(), p.getY(), true).getX() + dd / 2);
			int y = (int) (convertToScreenCoords(p.getX(), p.getY(), true).getY() + dd / 2);
			
			path.lineTo(x, y);
		}
		
		path.lineTo(endPoint.getX() + dd / 2, endPoint.getY() + dd / 2);
		drawPath = true;
		setPath(path, totalTime);
	}
		
	public void makeToast(String message, int duration) {
		Toast.makeText(getContext(), message, duration).show();
	}
	
	public void drawPlayer(float x, float y) {
		getPath().reset();
		invalidate();
		if(target != null && target.getX() == x && target.getY() == y)
			return;
		if(!obstacles.isEmpty()) {
			for(Obstacle o : obstacles) {
				if(x == o.getX() && y == o.getY())
					return;
			}
		}
		player = new Entity((int) x, (int) y, this);
		player.setDeleted(false);
		drawPlayer = true;
		invalidate();
	}
	
	public void drawObstacle(float x, float y) {
		getPath().reset();
		invalidate();
		if((target != null && target.getX() == x && target.getY() == y) || (player != null && player.getX() == x && player.getY() == y))
			return;
		for(Obstacle o : obstacles) {
			if(o.getX() == x && o.getY() == y) {
				obstacles.remove(o);
				invalidate();
				return;
			}
		}
		obstacles.add(new Obstacle((int) x, (int) y, this));
		drawObstacle = true;
		invalidate();
	}
	
	/**
	 * Передавать в координатах сетки
	 * 
	 * @param x
	 * @param y
	 * @return 
	 * 
	 */
	
	public void drawSideNode(SideNode sn) {
		boolean foundSameSideNode = false;
		
		for(SideNode s : sNodes) {
			if(s.getX() == sn.getX() && s.getY() == sn.getY()) {
				foundSameSideNode = true;
				s.closeSNode();
			}
		}
		
		if(!foundSameSideNode) {
			sNodes.add(new SideNode(sn.getX(), sn.getY(), sn.getCurrColor()));
		}
		
		drawSideNode = true;
		Point sp = convertToScreenCoords(sn.getX(), sn.getY(), true);
		int sx = (int) (sp.getX() - xoffset);
		int sy = (int) (sp.getY() - yoffset);
		Rect region = new Rect(sx, sy, sx + dd, sy + dd);
		invalidate(region);
	}
	
	public void generateSideNodes(ArrayList<SideNode> sNodes) {
		for(SideNode sn : sNodes) {
			drawSideNode(sn);
		}
	}
	
	public void drawEndPoint(float x, float y) {
		getPath().reset();
		invalidate();
		if(player != null && player.getX() == x && player.getY() == y)
			return;
		if(!obstacles.isEmpty()) {
			for(Obstacle o : obstacles) {
				if(x == o.getX() && y == o.getY())
					return;
			}
		}
		target = new Entity((int) x, (int) y, this);
		target.setDeleted(false);
		drawTarget = true;
		invalidate();
	}
	
	public void clearMap() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Вы уверены, что хотите всё стереть?")
			.setPositiveButton("Да", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getPath().reset();
					if(!obstacles.isEmpty())
						obstacles.clear();
					invalidate();
				}
			})
			.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		builder.create().show();
	}
	
	public void deletePoint(float x, float y) {
		if(!obstacles.isEmpty()) {
			for(Obstacle o : obstacles) {
				if(o.getX() == x && o.getY() == y) {
					obstacles.remove(o);
					break;
				}
			}
		}
		if(player == null || target == null) {
			invalidate();
			return;
		}
		if(player.getX() == x && player.getY() == y) {
			drawPlayer = false;
			player.setDeleted(true);
		}
		if(target.getX() == x && target.getY() == y) {
			drawTarget = false;
			target.setDeleted(true);
		}
		invalidate();
	}
	
	public ArrayList<Node> getNeighbours(Node node) {
		ArrayList<Node> neighbours = new ArrayList<Node>();
		
		for(int x = -1; x <= 1; x++) {
			for(int y = -1; y <= 1; y++) {
				if(x == 0 && y == 0) continue;
				if(!diagonal && (x == -1 || x == 1) && (y == -1 || y == 1)) continue;
				
				int checkX = node.getX() + x;
				int checkY = node.getY() + y;
				
				if(checkX > gridSize - 1 || checkY > gridSize - 1 || checkX < 0 || checkY < 0)
					continue;
					
				// Позиции в экранных координатах (временные переменные)
				Point st = convertToScreenCoords(checkX, checkY, true);
				int t1 = (int) st.getX();
				int t2 = (int) st.getY();
				
				if(t1 >= x0 && t1 < canvasWidth - xoffset && t2 <= canvasHeight - y0) {
					neighbours.add(new Node(checkX, checkY));
				}
			}
		}
		
		return neighbours;
	}
	
	public float calculateTotalDistance(ArrayList<Point> positions) {
		float d = 0;
		for(int i = 0; i < positions.size() - 1; i++) {
			d += eDistance(positions.get(i+1), positions.get(i));
		}
		return d;
	}
	
	public float eDistance(Point a, Point b) {
		Point ap = convertToScreenCoords(a.getX(), a.getY(), true);
		Point bp = convertToScreenCoords(b.getX(), b.getY(), true);
		float dx = ap.getX() - bp.getX();
		float dy = ap.getY() - bp.getY();
		return dx * dx + dy * dy;
	}
	
	/**
	 * 
	 * Передавать в координатах сетки
	 * 
	 * @param oldStartPosition
	 * @param oldTargetPosition
	 */
	
	public void revertAllPositions(Point oldStartPosition, Point oldTargetPosition, ArrayList<Obstacle> obst) {
		// int limit = N - (N % MIN_SIZE) - 1;
		int limit = gridSize - 1;
		ArrayList<Obstacle> oldObst = new ArrayList<Obstacle>();
		for(int i = 0; i <= obst.size() - 1; i++) {
			oldObst.add(obst.get(i));
		}
		Point p = convertToScreenCoords(oldStartPosition.getX(), oldStartPosition.getY(), true);
		// player = new Player(p[0], p[1], this);
		drawPlayer(p.getX(), p.getY());
		Point t = convertToScreenCoords(oldTargetPosition.getX(), oldTargetPosition.getY(), true);
		// target = new Target(p[0], p[1], this);
		drawEndPoint(t.getX(), t.getY());
		if(player.getGx() > limit) {
			player.setGx(limit);
			Point pPos = new Point(player.getX(), player.getY());
			drawPlayer(pPos.getX(), pPos.getY());
		}	
		if(player.getGy() > limit) {
			player.setGy(limit);
			Point pPos = new Point(player.getX(), player.getY());
			drawPlayer(pPos.getX(), pPos.getY());
		}	
		if(target.getGx() > limit)
			target.setGx(limit);
		if(target.getGy() > limit)
			target.setGy(limit);
		obstacles.clear();
		if(oldObst.isEmpty()) {
			invalidate();
			return;
		}
		for(Obstacle o : oldObst) {
			Point oc = convertToScreenCoords(o.getGx(), o.getGy(), true);
			// o.setPosition(oc[0], oc[1]);
			drawObstacle(oc.getX(), oc.getY());
		}
		for(int i = 0; i <= this.obstacles.size() - 1; i++) {
			if(this.obstacles.get(i).getGx() > limit || this.obstacles.get(i).getGy() > limit) {
				this.obstacles.remove(i);
				i = 0;
			}
		}
		drawPath = false;
		invalidate();
	}
	
	public void setGridSize(int n) {
		gridSize = n;
		dd = (canvasWidth > canvasHeight ? canvasHeight - y0 : canvasWidth - x0) / gridSize;
				
		xoffset = (canvasWidth - (gridSize * dd)) / 2;
		yoffset = (canvasHeight - (gridSize * dd)) / 2;
	}
	
	public Point convertToGridCoords(float x, float y) {
		return new Point(((int) x - xoffset) / dd, ((int) y + yoffset) / dd);
	}
	
	/**
	 * Переводит координаты сетки в координаты на экране
	 * 
	 * @param x Координата по оси X
	 * @param y Координата по оси Y
	 * @param mode true - перевод сетки в координаты экрана, false - перевод координат экрана в верхний левый угол клетки 
	 *  
	 */
	
	public Point convertToScreenCoords(float x, float y, boolean mode) {
		
		int dx, dy;
		
		if(mode) {
			dx = (int) (x * dd) + xoffset;
			dy = (int) (y * dd) + yoffset;
		}
		else {
			dx = (int) (x - (x % dd)) + xoffset;
			dy = (int) (Math.floor(y) - (Math.floor(y) % dd));
		}
		
		return new Point(dx, dy);
	}
	
	public void setDrawingMode(boolean mode) {
		drawPlayer = mode;
		drawTarget = mode;
		drawObstacle = mode;
	}
	
	// ------------------- Геттеры и сеттеры

	public int getX0() {
		return x0;
	}

	public int getTime() {
		return time;
	}
	
	public void setSpeed(int value) {
		animationSpeed = value;
	}
	
	public int getSpeed() {
		return animationSpeed;
	}

	public ArrayList<Point> getCheckpoints() {
		return checkpoints;
	}

	public void setCheckpoints(ArrayList<Point> checkpoints) {
		this.checkpoints = checkpoints;
	}

	public void setX0(int x0) {
		this.x0 = x0;
	}

	public int getCanvasWidth() {
		return canvasWidth;
	}

	public void setCanvasWidth(int canvasWidth) {
		this.canvasWidth = canvasWidth;
	}

	public int getCanvasHeight() {
		return canvasHeight;
	}

	public void setCanvasHeight(int canvasHeight) {
		this.canvasHeight = canvasHeight;
	}

	public int getXoffset() {
		return xoffset;
	}

	public void setXoffset(int xoffset) {
		this.xoffset = xoffset;
	}

	public int getYoffset() {
		return yoffset;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public void setYoffset(int yoffset) {
		this.yoffset = yoffset;
	}
	
	public boolean isEDistance() {
		return eDistance;
	}

	public void setEDistance(boolean mDistance) {
		this.eDistance = mDistance;
	}

	public int getDd() {
		return dd;
	}

	public void setDd(int dd) {
		this.dd = dd;
	}

	public ArrayList<Obstacle> getObstacles() {
		return obstacles;
	}

	public boolean isEnableNeighbours() {
		return enableNeighbours;
	}

	public void setEnableNeighbours(boolean enableNeighbours) {
		this.enableNeighbours = enableNeighbours;
	}

	public int getPlayerColor() {
		return playerColor;
	}
	
	public int getPathColor() {
		return pathColor;
	}

	public void setPathColor(int pathColor) {
		this.pathColor = pathColor;
	}

	public void setPlayerColor(int playerColor) {
		this.playerColor = playerColor;
		invalidate();
	}

	public int getTargetColor() {
		return targetColor;
	}
	
	public void setClassic(boolean classic) {
		this.classic = classic;
		invalidate();
	}
	
	public boolean getClassic() {
		return classic;
	}

	public Entity getPlayer() {
		return player;
	}

	public void setPlayer(Entity player) {
		this.player = player;
	}

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public void setObstacles(ArrayList<Obstacle> obstacles) {
		this.obstacles = obstacles;
	}

	public void setTargetColor(int targetColor) {
		this.targetColor = targetColor;
		invalidate();
	}

	public void setInfo(int iterations, int time) {
		this.iterations = iterations;
		this.time = time;
	}

	public int getIterations() {
		return iterations;
	}
	
	public float getAngle() {
		return angle;
	}
	
	public void setDiagonal(boolean value) { 
		this.diagonal = value;
	}

	public int getGridSize() {
		return gridSize;
	}

}

/* ------------------------------------



---------------------------------------*/ 