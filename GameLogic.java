package drawtogether;

import java.util.Random;
import java.util.ArrayList;
import java.awt.Color;

import drawtogether.Ball.Facing;

/**
 * GameLogic class defines how the game behaves in response to user control and channel message.
 */
public class GameLogic {
	
	/**
	 * Game setting constants on basic ball and coins' radius and colors.
	 */
	public static final int COIN_RADIUS = 5;
	public static final int BALL_RADIUS = 10; 
	public static final Color MAIN_BALL_COLOR = Color.YELLOW;
	public static final Color OTHER_BALL_COLOR = Color.ORANGE;
	public static final Color COIN_COLOR = Color.RED;
	public static final Color COIN_COLOR_2 = Color.PINK;
	public static final Color COIN_COLOR_3 = Color.BLUE;
	public static final Color COIN_COLOR_4 = Color.YELLOW;
	
	/**
	 * Balls that are currently in the game.
	 */
	private ArrayList<Ball> Balls = new ArrayList<Ball>();
	
	/**
	 * Coins that are currently in the game.
	 */
	private ArrayList<Ball> Coins = new ArrayList<Ball>();
	
	/**
	 * Number of coins to regenerate once all coins are eaten.
	 */
	private int numCoins = 3;
	
	/**
	 * JFrame, GamePanel, BallController of the local ball and the GameConnector of the game.
	 */
	private DrawFrame dframe;
	private GamePanel gpanel;
	private BallController bc;
	private GameConnector gamecon;
	
	/**
	 * Balls getter.
	 * 
	 * @return  a list of balls currently in the game
	 */
	public ArrayList<Ball> getBalls() {
		return this.Balls;
	}
	
	/**
	 * Coins getter.
	 * 
	 * @return  a list of coins currently in the game
	 */
	public ArrayList<Ball> getCoins() {
		return this.Coins;
	}
	
	/**
	 * Class constructor.
	 * Initiates the main ball which the user can control, the ball controller to the main ball.
	 * Publish local states and readiness flag to channel.
	 * 
	 * @param panel  GamePanel of the game
	 * @param dframe  JFrame of the game
	 * @param gamecon  GameConenctor of the game
	 */
	public GameLogic(GamePanel panel, DrawFrame dframe, GameConnector gamecon) {
		
		this.dframe = dframe;
		this.gpanel = panel;
		this.gamecon = gamecon;
		
		Ball mball = new Ball(BALL_RADIUS, (int)(this.dframe.getWidth()/2), (int)(this.dframe.getHeight()/2),
				MAIN_BALL_COLOR, dframe.gamecon.userName, true);
		this.bc = new BallController(mball, dframe);
		this.gpanel.addKeyListener(this.bc);
		this.Balls.add(mball);
		
		if(gamecon.isHost) {
			for(int i = 0; i < this.numCoins; i++) {
				Ball coin = randomGenerateBall(COIN_RADIUS);
				coin.setColor(COIN_COLOR);
				Random random = new Random();
				int color = random.nextInt(3) + i + 1;
	            coin.setScore(color);
				this.Coins.add(coin);
			}
		}
		
		gamecon.sendNewBallCoord(mball);
		gamecon.sendReadyToPlay();
	}
	
	/**
	 * Update game states based on user control input and channel message received.
	 */
	public void Update() {
		boolean flag = false;
		ArrayList<Ball> toRemoveCoin = new ArrayList<Ball>();
		ArrayList<Ball> toAddCoin = new ArrayList<Ball>();
		ArrayList<Ball> toRemoveBall = new ArrayList<Ball>();

		int pos = 0;
		
		for(Ball fb : Balls) {
			pos ++;
			
			// update ball position
			if(fb.move()) {
				flag = true;
			}
			
			// remove coins eaten
			for(Ball c : Coins) {
				if(isOverlap(fb, c)) {
					toRemoveCoin.add(c);	

					if(fb == this.getMBall()) {
						fb.absorbCoin();
						this.gamecon.sendNewBallScore(fb);
					}
				}
			}
			fb.adjustShape(BALL_RADIUS);
			
			// minimize for loop
			if(pos == Balls.size()) break;
			
			// reset position of balls eaten by another ball
			for(Ball sb : Balls.subList(pos, Balls.size())) {
				if(isOverlap(fb, sb)) {
					if(fb.isLocal()) {
						if(fb.getR() <= sb.getR()) {
							sb.absorbCoins((int)(fb.getCurrentCoins()/2));
							this.gamecon.sendNewBallScore(sb);
							mainBallReincarnate(fb);
					    }
						if(sb.getR() <= fb.getR()) {
							otherBallReincarnate(sb);
					    }		
					} else if (sb.isLocal()) {
						if(sb.getR() <= fb.getR()) {
							fb.absorbCoins((int)(sb.getCurrentCoins()/2));
							this.gamecon.sendNewBallScore(fb);
							mainBallReincarnate(sb);
					    }
						if(fb.getR() <= sb.getR()) {
							otherBallReincarnate(fb);
					    }
					} else {
						if(sb.getR() <= fb.getR()) {
							otherBallReincarnate(sb);
					    }
						if(fb.getR() <= sb.getR()) {
							otherBallReincarnate(fb);
					    }
					}
				}
			}
		}
		this.bc.adjustSpeed(BALL_RADIUS);
		
		// regenerate coins if no more coin
		if(this.Coins.isEmpty() && gamecon.isHost) {
			for(int i = 0; i < numCoins; i++) {
				Ball new_coin = randomGenerateBall(COIN_RADIUS);
				Random random = new Random();
				int color = random.nextInt(3) + i + 1;
	            new_coin.setScore(color);
				toAddCoin.add(new_coin);
			}
			gamecon.sendNewCoinCoord(toAddCoin);
		}
		
		// update states
		for(Ball c : toRemoveCoin) {this.Coins.remove(c);}
		for(Ball c : toAddCoin) {this.Coins.add(c);}
		for(Ball b : toRemoveBall) {this.Balls.remove(b);}
		
		// publish main ball coordinates
		if(flag)
			gamecon.sendNewBallCoord(getMBall());
	}
	
	/**
	 * The main ball getter.
	 * 
	 * @return  the ball object which local user is controlling
	 */
	public Ball getMBall() {
		for(Ball b : this.Balls) {
			if(b.isLocal()) return b;
		}
		return null;
	}
	
	/**
	 * Ask GameConnector to send message to inform the leaving of the sender host on the channel.
	 */
	public void sendHostExit() {
		for(Ball ball : Balls) {
			if(!ball.getName().equals(gamecon.userName)) {
				gamecon.sendHostExit(ball.getName());
				break;
			}
		}
	}
	
	/**
	 * Set the ball to its new position and reset its current score.
	 * 
	 * @param coord  coordinates of the new position
	 * @param username  the name of the ball object to be repositioned
	 */
	public void setOtherBallPosition(String[] coord, String username) {
		if(coord.length < 3) {
			return;
		}
		for (Ball ball : this.Balls) 
			if(ball.getName().equals(username)) {
				if(ball.isLocal()) {
					return;
				}
				ball.setX(Integer.valueOf(coord[1]));
				ball.setY(Integer.valueOf(coord[2]));
				ball.setColor(OTHER_BALL_COLOR);
				ball.setR(BALL_RADIUS);
				ball.resetCoinCurrent();
				break;
		}
	}
	
	/**
	 * Set the all time score of the ball.
	 * 
	 * @param score  the score to be set
	 * @param username  the name of the ball object to be assigned a new score
	 */
	public void setOtherBallScore(int score, String username) {
		for (Ball ball : this.Balls) {
			if(ball.getName().equals(username)) {
				ball.setScore(score);
				break;
			}	
		}
	}
	
	/**
	 * Set the current score of the ball.
	 * 
	 * @param current  the score to be set
	 * @param username  the name of the ball object to be assigned a new score
	 */
	public void setOtherBallCurrentCoins(int current, String username) {
		for (Ball ball : this.Balls) {
			if(ball.getName().equals(username)) {
				if(ball.isLocal()) {
					return;
				}
				ball.setCurrentCoins(current);;
				break;
			}	
		}
	}
	
	/**
	 * Remove a ball from the game.
	 * 
	 * @param uuid  UUID of the host that is controlling the ball
	 */
	public void removeOtherBall(String uuid) {
		Ball toRemove = null;
		for (Ball ball : this.Balls) 
    		if(ball.getName().equals(this.gamecon.participants.get(uuid))) {
    			if(ball.isLocal()) {
    				return;
    			}
				toRemove = ball;
				break;
    		}  	
		if(toRemove != null)
		this.Balls.remove(toRemove);
	}
	
	/**
	 * Update the ball position and facing direction.
	 * If not existed yet, create a new ball.
	 * 
	 * @param str  array containing the ball coordinates, in the form of ["Ball" or "Coin", x, y]
	 * @param username  the name of the ball object to be updated
	 */
	public void updateOtherBall(String[] str, String username) {		
    	boolean ball_existed = false;  	
    	for (Ball ball : this.Balls) 
    		if(ball.getName().equals(username)) {
    			if(ball.isLocal()) {
    				return;
    			}
    			int dx = Integer.valueOf(str[1]) - ball.getX();
    			int dy = Integer.valueOf(str[2]) - ball.getY();
    			if(dx > 0) {
    				if(dy > 0) {
    					ball.setFace(Facing.BOTTOMRIGHT);
    				} else if (dy < 0) {
    					ball.setFace(Facing.TOPRIGHT);
    				} else {
    					ball.setFace(Facing.RIGHT);
    				}
    			} else if (dx < 0){
    				if(dy > 0) {
    					ball.setFace(Facing.BOTTOMLEFT);
    				} else if (dy < 0) {
    					ball.setFace(Facing.TOPLEFT);
    				} else {
    					ball.setFace(Facing.LEFT);
    				}
    			} else {
    				if(dy > 0) {
    					ball.setFace(Facing.BOTTOM);
    				} else {
    					ball.setFace(Facing.TOP);
    				}
    			}
				ball.setX(Integer.valueOf(str[1]));
				ball.setY(Integer.valueOf(str[2]));
				ball.setColor(GameLogic.OTHER_BALL_COLOR);
				ball_existed = true;
				break;
    		}  	
    	if(!ball_existed) {
    		Ball ball = new Ball(GameLogic.BALL_RADIUS, Integer.valueOf(str[1]), Integer.valueOf(str[2])
    				, GameLogic.OTHER_BALL_COLOR, username, false);
    		this.Balls.add(ball);
    	}
	}
	
	/**
	 * Update coins' coordinates based on message from the main server.
	 * Assign a color to the coin in addition. (Using coin_absorbed attribute of the coin object)
	 * 
	 * @param str  array containing the coordinates, in the form of ["Coin", x, y, "Coin", ...]
	 */
	public void updateCoins(String[] str) {
    	if(!this.gamecon.isHost) {
    		this.Coins.clear();
    		for (int i = 0; i + 2 < str.length; i = i + 3) {
    			Ball coin = new Ball(GameLogic.COIN_RADIUS, Integer.valueOf(str[i+1]), Integer.valueOf(str[i+2]),
    					COIN_COLOR, this.gamecon.userName, true);
    			Random random = new Random();
				int color = random.nextInt(3) + i/3 + 1;
	            coin.setScore(color);
    			this.Coins.add(coin);
    		}
    	}
	}
	
	/**
	 * Reallocate the main ball and reset its current score in case of being eaten.
	 * Publish the new coordinates of the main ball to the channel.
	 * 
	 * @param mb  ball object representing the main ball local user is controlling
	 */
	private void mainBallReincarnate(Ball mb) {
		coordinates coord = randomGenerateCoordinates();
		mb.setX(coord.x);
		mb.setY(coord.y);
		mb.setR(BALL_RADIUS);
		mb.resetCoinCurrent();
		mb.setColor(MAIN_BALL_COLOR);
		this.gamecon.sendBallResetPos(mb);
	}
	
	/**
	 * Reallocate a ball and reset its current score in case of being eaten.
	 * 
	 * @param ob  ball object to be reallocated
	 */
	private void otherBallReincarnate(Ball ob) {
		coordinates coord = new coordinates(-4*BALL_RADIUS, -4*BALL_RADIUS);
		ob.setX(coord.x);
		ob.setY(coord.y);
		ob.setR(BALL_RADIUS);
		ob.resetCoinCurrent();
		ob.setColor(OTHER_BALL_COLOR);
	}
	
	/**
	 * Generate a random pair of coordinate within the JFrame boundary.
	 * 
	 * @return  a coordinates object wit random values within JFrame boundary
	 */
	private coordinates randomGenerateCoordinates() {
		Random random = new Random();
	    int _x = 8*BALL_RADIUS + random.nextInt(this.dframe.getWidth() - 16*BALL_RADIUS);
	    int _y = 8*BALL_RADIUS + random.nextInt(this.dframe.getHeight() - 16*BALL_RADIUS);
		return new coordinates(_x, _y);
	}

	/**
	 * Generate a ball object at a random position within the JFrame.
	 * 
	 * @param radius  radius of the newly generated ball object  
	 * @return  a ball object generated at a random position
	 */
	private Ball randomGenerateBall(int radius) {
		Random random = new Random();
	    int _x = 8*radius + random.nextInt(this.dframe.getWidth() - 16*radius);
	    int _y = 8*radius + random.nextInt(this.dframe.getHeight() - 16*radius); 	
		return new Ball(radius, _x, _y);
	}
	
	/**
	 * Check if two ball object overlaps completely.
	 * 
	 * @param b1  the first ball object
	 * @param b2  the second ball object
	 * @return  true of false if the two ball objects overlap
	 */
	private boolean isOverlap(Ball b1, Ball b2) {
		int largerR = b1.getR() > b2.getR() ? b1.getR() : b2.getR();
		int smallerR = b1.getR() == largerR ? b2.getR() : b1.getR();
		double dist = dist(b1.getX(), b1.getY(), b2.getX(), b2.getY());
		return (double)largerR > (dist + smallerR);
	}
	
	/**
	 * Calculate the distance between two points.
	 * 
	 * @param x1  horizontal coordinate of the first point
	 * @param y1  vertical coordinate of the first point
	 * @param x2  horizontal coordinate of the second point
	 * @param y2  vertical coordinate of the second point
	 * @return  the distance between the two pints
	 */
	private double dist(int x1, int y1, int x2, int y2) {
		return Math.hypot(x1-x2, y1-y2);
	}
		
	/**
	 * Coordinates class represents a pair of integer coordinate
	 */
	public class coordinates {
		public final int x;
		public final int y;	
		
		/**
		 * Class constructor.
		 * 
		 * @param x  horizontal coordinate
		 * @param y  vertical coordinate
		 */
		public coordinates(int x, int y) {
			this.x = x;
			this.y = y;
		}		
	}
}
