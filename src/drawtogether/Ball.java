package drawtogether;

import java.awt.Color;

/**
 * Ball is the basic class that represent balls and coins that appear in this game.
 */
public class Ball {
	/**
	 * The base unit of each ball size increment.
	 */
	private static final int increment = 4;
	
	/**
	 * The radius, horizontal coordinate and vertical coordinate of the ball object.
	 */
	private int radius;
	private int x;
	private int y;
	
	/**
	 * The horizontal and vertical offset on which the ball object is moving based.
	 */
	private int dx = 0;
	private int dy = 0;
	
	
	/**
	 * The direction that the ball object is facing.
	 */
	private Facing face;
	
	/**
	 * The flag indicating whether the ball object generated locally or based on a channel message.
	 */
	private boolean local;
	
	/**
	 * The color of the ball object.
	 */
	private Color color;
	
	/**
	 * The name of the ball object.
	 */
	private String name;
	
	/**
	 * The total score of the ball object.
	 */
	private int coin_absorbed = 0;
	
	/**
	 * The current score of the ball object.
	 */
	private int coin_absorbed_current = 0;
	
	public enum Facing {RIGHT, LEFT, TOP, BOTTOM, TOPRIGHT, TOPLEFT, BOTTOMRIGHT, BOTTOMLEFT};
	
	/**
	 * Class constructor specifying radius and horizontal and vertical coordinates of the ball object.
	 * 
	 * @param r  radius
	 * @param x  horizontal coordinate
	 * @param y  vertical coordinate
	 */
	public Ball(int r, int x, int y) {
		this.radius = r;
		this.x = x;
		this.y = y;
		this.color = Color.RED;
		this.local = true;
		this.face = Facing.RIGHT;
	}

	/**
	 * Class constructor specifying radius, horizontal and vertical coordinates and color of the ball object.
	 * 
	 * @param r  radius
	 * @param x  horizontal coordinate
	 * @param y  vertical coordinate
	 * @param c  color
	 */
	public Ball(int r, int x, int y, Color c) {
		this.radius = r;
		this.x = x;
		this.y = y;
		this.color = c;
		this.local = true;
		this.face = Facing.RIGHT;
	}

	/**
	 * Class constructor specifying radius, horizontal and vertical coordinates, color 
	 * and the locality flag of the ball object.
	 * 
	 * @param r  radius
	 * @param x  horizontal coordinate
	 * @param y  vertical coordinate
	 * @param c  color
	 * @param isLocal  locality flag
	 */
	public Ball(int r, int x, int y, Color c, boolean isLocal) {
		this.radius = r;
		this.x = x;
		this.y = y;
		this.color = c;
		this.local = isLocal;
		this.face = Facing.RIGHT;
	}
	
	/**
	 * Class constructor specifying radius, horizontal and vertical coordinates, color, user name 
	 * and the locality flag of the ball object.
	 * 
	 * @param r  radius
	 * @param x  horizontal coordinate
	 * @param y  vertical coordinate
	 * @param c  color
	 * @param name  user name that is controlling the ball object
	 * @param isLocal  locality flag
	 */
	public Ball(int r, int x, int y, Color c, String name, boolean isLocal) {
		this.radius = r;
		this.x = x;
		this.y = y;
		this.color = c;
		this.name = name;
		this.local = isLocal;
		this.face = Facing.RIGHT;
	}
	
	/**
	 * The locality flag getter.
	 * 
	 * @return  value of the locality flag of the ball object
	 */
	public boolean isLocal() {
		return this.local;
	}
	
	/**
	 * Update the coordinates and facing direction of the ball object
	 * 
	 * @return  whether the ball object has moved.
	 */
	public boolean move() {
		if(!this.local){
			return false;
		}
		this.x += dx;
		this.y += dy;
		if(dx == 0 && dy == 0) {
			return false;
		}
		if(dx > 0) {
			if(dy > 0) {
				this.face = Facing.BOTTOMRIGHT;
			} else if (dy < 0) {
				this.face = Facing.TOPRIGHT;
			} else {
				this.face = Facing.RIGHT;
			}
		} else if (dx < 0){
			if(dy > 0) {
				this.face = Facing.BOTTOMLEFT;
			} else if (dy < 0) {
				this.face = Facing.TOPLEFT;
			} else {
				this.face = Facing.LEFT;
			}
		} else {
			if(dy > 0) {
				this.face = Facing.BOTTOM;
			} else {
				this.face = Facing.TOP;
			}
		}
		return true;
	}
	
	/**
	 * The horizontal coordinate getter.
	 * 
	 * @return  value of the horizontal coordinate of the ball object
	 */
	public int getX() {
		return this.x;
	}
	
	/**
	 * The horizontal coordinate setter.
	 * 
	 * @param x  horizontal coordinate of the ball object
	 */
	public void setX(int x) {
		this.x = x;
	}
	
	/**
	 * The vertical coordinate getter.
	 * 
	 * @return  value of the vertical coordinate of the ball object
	 */
	public int getY() {
		return this.y;
	}
	
	/**
	 * The vertical coordinate setter.
	 * 
	 * @param y  vertical coordinate of the ball object
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	/**
	 * The radius getter.
	 * 
	 * @return  value of the radius of the ball object
	 */
	public int getR() {
		return this.radius;
	}

	/**
	 * The radius setter.
	 * 
	 * @param radius  radius of the ball object
	 */
	public void setR(int radius) {
		this.radius = radius;
	}
	
	/**
	 * The color getter.
	 * 
	 * @return  value of the color of the ball object
	 */
	public Color getColor() {
		return this.color;
	}
	
	/**
	 * The color setter.
	 * 
	 * @param color  color of the ball object
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	/**
	 * The facing direction getter.
	 * 
	 * @return  value of the facing direction of the ball object
	 */
	public Facing getFace() {
		return this.face;
	}
	
	/**
	 * The facing direction setter.
	 * 
	 * @param face  facing direction of the ball object
	 */
	public void setFace(Facing face) {
		this.face = face;
	}
	
	/**
	 * The horizontal offset setter.
	 * 
	 * @param offsetx  horizontal offset the ball object is to move
	 */
	public void moveX(int offsetx) {
		this.dx = offsetx;
	}
	
	/**
	 * The vertical offset setter.
	 * 
	 * @param offsety  vertical offset the ball object is to move
	 */
	public void moveY(int offsety) {
		this.dy = offsety;
	}
	
	/**
	 * The horizontal and vertical offset setter.
	 * 
	 * @param offsetx  horizontal offset the ball object is to move
	 * @param offsety  vertical offset the ball object is to move
	 */
	public void moveXY(int offsetx, int offsety) {
		this.dx = offsetx;
		this.dy = offsety;
	}
	
	/**
	 * Return the horizontal and vertical coordinates of the ball object.
	 * 
	 * @return formatted string presenting the horizontal and vertical coordinates of the ball object
	 */
	public String getXY_Str() {
		return "Ball,"+Integer.toString(x)+','+Integer.toString(y);
	}
	
	/**
	 * Return the horizontal and vertical coordinates of the coin.
	 * 
	 * @return formatted string presenting the horizontal and vertical coordinates of the coin
	 */
	public String getXY_Str_coin() {
		return "Coin,"+Integer.toString(x)+','+Integer.toString(y)+',';
	}
	
	/**
	 * The name getter.
	 * 
	 * @return  the name of the ball object
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Increment both all time score and current score by 1.
	 */
	public void absorbCoin() {
		this.coin_absorbed ++;
		this.coin_absorbed_current ++;
	}
	
	/**
	 * Increment both all time score and current score.
	 * 
	 * @param coins  the amount of score to increase
	 */
	public void absorbCoins(int coins) {
		this.coin_absorbed += coins;
		this.coin_absorbed_current += coins;
	}
	
	/**
	 * Reset the current score of the ball object.
	 */
	public void resetCoinCurrent() {
		this.coin_absorbed_current = 0;
	}
	
	/**
	 * The current score getter.
	 * 
	 * @return the value of the current score of the ball object
	 */
	public int getCurrentCoins() {
		return this.coin_absorbed_current;
	}
	
	/**
	 * The current score setter.
	 * 
	 * @param current  current score of the ball object
	 */
	public void setCurrentCoins(int current) {
		this.coin_absorbed_current = current;
	}
	
	/**
	 * The all time score getter.
	 * 
	 * @return the value of the all time score of the ball object
	 */
	public int getScore() {
		return this.coin_absorbed;
	}
	
	/**
	 * The all time score setter.
	 * 
	 * @param score  all time score of the ball object
	 */
	public void setScore(int score) {
		this.coin_absorbed = score;
	}
	
	/**
	 * Adjust the radius of the ball object based on its current score.
	 * Maximum increase 5 times.
	 * 
	 * @param baseRadius  the smallest possible radius of a ball object
	 */
	public void adjustShape(int baseRadius) {
		if(this.coin_absorbed_current == 0) {
			this.radius = baseRadius;
			return;
		}
		double lvl = Math.log((double)this.coin_absorbed_current);
		lvl = lvl > 5 ? 5 : lvl;
		this.radius = baseRadius + increment * (int)lvl;
	}
}