package drawtogether;

import java.awt.Color;

public class Ball {
	private static final int increment = 3;
	
	private int radius;
	private int x;
	private int y;
	private int dx;
	private int dy;
	private boolean local;
	private Color color;
	private String name;
	private int coin_absorbed = 0;
	private int coin_absorbed_current = 0;
	
	public Ball(int r, int x, int y) {
		this.radius = r;
		this.x = x;
		this.y = y;
		this.color = Color.RED;
		this.local = true;
	}

	public Ball(int r, int x, int y, Color c) {
		this.radius = r;
		this.x = x;
		this.y = y;
		this.color = c;
		this.local = true;
	}

	public Ball(int r, int x, int y, Color c, boolean isLocal) {
		this.radius = r;
		this.x = x;
		this.y = y;
		this.color = c;
		this.local = isLocal;
	}
	
	public Ball(int r, int x, int y, Color c, String name, boolean isLocal) {
		this.radius = r;
		this.x = x;
		this.y = y;
		this.color = c;
		this.name = name;
		this.local = isLocal;
	}
	
	public boolean isLocal() {
		return this.local;
	}
	
	public boolean move() {
		if(!this.local){
			return false;
		}
		this.x += dx;
		this.y += dy;
		if(dx == 0 && dy == 0) {
			return false;
		}
		return true;
	}
	
	public int getX() {
		return this.x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getR() {
		return this.radius;
	}

	public void setR(int radius) {
		this.radius = radius;
	}
	
	public Color getColor() {
		return this.color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public void moveX(int offsetx) {
		this.dx = offsetx;
	}
	
	public void moveY(int offsety) {
		this.dy = offsety;
	}
	
	public void moveXY(int offsetx, int offsety) {
		this.dx = offsetx;
		this.dy = offsety;
	}
	
	public String getXY_Str() {
		return "Ball,"+Integer.toString(x)+','+Integer.toString(y);
	}
	
	public String getXY_Str_coin() {
		return "Coin,"+Integer.toString(x)+','+Integer.toString(y)+',';
	}
	
	public String getName() {
		return name;
	}
	
	public void absorbCoin() {
		this.coin_absorbed ++;
		this.coin_absorbed_current ++;
	}
	
	public void absorbCoins(int coins) {
		this.coin_absorbed += coins;
		this.coin_absorbed_current += coins;
	}
	
	public void resetCoinCurrent() {
		this.coin_absorbed_current = 0;
	}
	
	public int getCurrentCoins() {
		return this.coin_absorbed_current;
	}
	
	public void setCurrentCoins(int current) {
		this.coin_absorbed_current = current;
	}
	
	public int getScore() {
		return this.coin_absorbed;
	}
	
	public void setScore(int score) {
		this.coin_absorbed = score;
	}
	
	public void adjustShape(int baseRadius) {
		if(this.coin_absorbed_current == 0) {
			this.radius = baseRadius;
			return;
		}
		double lvl = Math.log((double)this.coin_absorbed_current);
		this.radius = baseRadius + increment * (int)lvl;
	}
}
