package drawtogether;

import java.util.Random;
import java.util.ArrayList;
import java.awt.Color;

public class GameLogic {
	
	public static final int COIN_RADIUS = 5;
	public static final int BALL_RADIUS = 10; 
	public static final Color MAIN_BALL_COLOR = Color.YELLOW;
	public static final Color OTHER_BALL_COLOR = Color.ORANGE;
	public static final Color COIN_COLOR = Color.RED;
	public static final Color COIN_COLOR_2 = Color.PINK;
	public static final Color COIN_COLOR_3 = Color.BLUE;
	public static final Color COIN_COLOR_4 = Color.YELLOW;
	
	private ArrayList<Ball> Balls = new ArrayList<Ball>();
	private ArrayList<Ball> Coins = new ArrayList<Ball>();
	private int numCoins = 3;
	private DrawFrame dframe;
	private GamePanel gpanel;
	private BallController bc;
	private GameConnector gamecon;
	
	public ArrayList<Ball> getBalls() {
		return this.Balls;
	}
	
	public ArrayList<Ball> getCoins() {
		return this.Coins;
	}
	
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
				Ball coin = randomGenerateBall(COIN_RADIUS, this.dframe);
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
	
	public void Update() {
		boolean flag = false;
		ArrayList<Ball> toRemoveCoin = new ArrayList<Ball>();
		ArrayList<Ball> toAddCoin = new ArrayList<Ball>();
		ArrayList<Ball> toRemoveBall = new ArrayList<Ball>();

		int pos = 0;
		
		for(Ball fb : Balls) {
			pos ++;
			if(fb.move()) {
				flag = true;
			}
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
			
			if(pos == Balls.size()) break;
			
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
		
		if(this.Coins.isEmpty() && gamecon.isHost) {
			for(int i = 0; i < numCoins; i++) {
				Ball new_coin = randomGenerateBall(COIN_RADIUS, this.dframe);
				Random random = new Random();
				int color = random.nextInt(3) + i + 1;
	            new_coin.setScore(color);
				toAddCoin.add(new_coin);
			}
			gamecon.sendNewCoinCoord(toAddCoin);
		}
		
		for(Ball c : toRemoveCoin) {this.Coins.remove(c);}
		for(Ball c : toAddCoin) {this.Coins.add(c);}
		for(Ball b : toRemoveBall) {this.Balls.remove(b);}
		
		if(flag)
			gamecon.sendNewBallCoord(getMBall());
	}
	
	private void mainBallReincarnate(Ball mb) {
		coordinates coord = randomGenerateCoordinates();
		mb.setX(coord.x);
		mb.setY(coord.y);
		mb.setR(BALL_RADIUS);
		mb.resetCoinCurrent();
		mb.setColor(MAIN_BALL_COLOR);
		this.gamecon.sendBallResetPos(mb);
	}
	
	private void otherBallReincarnate(Ball ob) {
		coordinates coord = new coordinates(-4*BALL_RADIUS, -4*BALL_RADIUS);
		ob.setX(coord.x);
		ob.setY(coord.y);
		ob.setR(BALL_RADIUS);
		ob.resetCoinCurrent();
		ob.setColor(OTHER_BALL_COLOR);
	}
	
	public coordinates randomGenerateCoordinates() {
		Random random = new Random();
	    int _x = 8*BALL_RADIUS + random.nextInt(dframe.getWidth() - 16*BALL_RADIUS);
	    int _y = 8*BALL_RADIUS + random.nextInt(dframe.getHeight() - 16*BALL_RADIUS);
		return new coordinates(_x, _y);
	}

	public static Ball randomGenerateBall(int radius, DrawFrame dframe) {
		Random random = new Random();
	    int _x = 8*radius + random.nextInt(dframe.getWidth() - 16*radius);
	    int _y = 8*radius + random.nextInt(dframe.getHeight() - 16*radius); 	
		return new Ball(radius, _x, _y);
	}
	
	private boolean isOverlap(Ball b1, Ball b2) {
		int largerR = b1.getR() > b2.getR() ? b1.getR() : b2.getR();
		int smallerR = b1.getR() == largerR ? b2.getR() : b1.getR();
		double dist = dist(b1.getX(), b1.getY(), b2.getX(), b2.getY());
		return (double)largerR > (dist + smallerR);
	}
	
	private double dist(int x1, int y1, int x2, int y2) {
		return Math.hypot(x1-x2, y1-y2);
	}
	
	public Ball getMBall() {
		for(Ball b : this.Balls) {
			if(b.isLocal()) return b;
		}
		return null;
	}
	
	public class coordinates {
		public final int x;
		public final int y;
		public coordinates(int x, int y) {
			this.x = x;
			this.y = y;
		}		
	}
	
	public void sendHostExit() {
		for(Ball ball : Balls)
			if(!ball.getName().equals(gamecon.userName))
				{gamecon.sendHostExit(ball.getName());
				break;
				}
	}
}
