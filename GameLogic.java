package drawtogether;

import java.util.Random;

import javax.swing.event.EventListenerList;

import org.umundo.core.Message;

import java.util.ArrayList;

import java.awt.Color;
import java.awt.event.KeyListener;

public class GameLogic {
	
	public static final int COIN_RADIUS = 5;
	public static final int BALL_RADIUS = 10; 
	public static final Color MAIN_BALL_COLOR = Color.RED;
	public static final Color COIN_COLOR = Color.YELLOW;
	public static final Color OTHER_BALL_COLOR = Color.BLUE;
	
	private ArrayList<Ball> Balls = new ArrayList<Ball>();
	private ArrayList<Ball> Coins = new ArrayList<Ball>();
	private int numCoins = 3;
	private DrawFrame dframe;
	private GamePanel gpanel;
	private BallController bc;
	private CoreChat corechat;
	
	public ArrayList<Ball> getBalls() {
		return this.Balls;
	}
	
	public ArrayList<Ball> getCoins() {
		return this.Coins;
	}
	
	public GameLogic(GamePanel panel, DrawFrame dframe, CoreChat corechat) {
		
		this.dframe = dframe;
		this.gpanel = panel;
		this.corechat = corechat;
		
		Ball mball = new Ball(BALL_RADIUS, (int)(this.dframe.getWidth()/2), (int)(this.dframe.getHeight()/2),
				MAIN_BALL_COLOR, dframe.corechat.userName, true);
		this.bc = new BallController(mball, dframe);
		this.gpanel.addKeyListener(this.bc);
		this.Balls.add(mball);
		
		if(corechat.isHost) {
			for(int i = 0; i < this.numCoins; i++) {
				Ball coin = randomGenerateBall(COIN_RADIUS, this.dframe);
				coin.setColor(COIN_COLOR);
				this.Coins.add(coin);
			}
		}
		
		corechat.sendNewBallCoord(mball);
		corechat.sendReadyToPlay();
	}
	
	public void Update() {
		boolean flag = false;
		ArrayList<Ball> toRemoveCoin = new ArrayList<Ball>();
		ArrayList<Ball> toAddCoin = new ArrayList<Ball>();
		ArrayList<Ball> toRemoveBall = new ArrayList<Ball>();
		ArrayList<Ball> toAddBall = new ArrayList<Ball>();
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
						this.corechat.sendNewBallScore(fb);
					}
				}
			}
			fb.adjustShape(BALL_RADIUS);
			
			if(pos == Balls.size()) break;
			
			for(Ball sb : Balls.subList(pos, Balls.size())) {
				if(isOverlap(fb, sb)) {
					System.out.println("Overlap:" + fb.getName() + " " + sb.getName());
					if(fb.isLocal()) {
						if(fb.getR() <= sb.getR()) {
							sb.absorbCoins((int)(fb.getCurrentCoins()/2));
							this.corechat.sendNewBallScore(sb);
							mainBallReincarnate(fb);
							System.out.println("main ball is eaten");
					    }
						if(sb.getR() <= fb.getR()) {
							//fb.setScore(fb.getScore() + (int)(sb.getCurrentCoins()/2));
							//this.corechat.sendNewBallScore(fb);
							otherBallReincarnate(sb);
					    	System.out.println("other ball is eaten");
					    }		
					} else if (sb.isLocal()) {
						if(sb.getR() <= fb.getR()) {
							fb.absorbCoins((int)(sb.getCurrentCoins()/2));
							this.corechat.sendNewBallScore(fb);
							mainBallReincarnate(sb);
							System.out.println("main ball is eaten");
					    }
						if(fb.getR() <= sb.getR()) {
							//sb.setScore(sb.getScore() + (int)(fb.getCurrentCoins()/2));
							//this.corechat.sendNewBallScore(sb);
							otherBallReincarnate(fb);
					    	System.out.println("other ball is eaten");
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
		
		if(this.Coins.isEmpty() && corechat.isHost) {
			for(int i = 0; i < numCoins; i++) {
				Ball new_coin = randomGenerateBall(COIN_RADIUS, this.dframe);
				new_coin.setColor(COIN_COLOR);
				toAddCoin.add(new_coin);
			}
			corechat.sendNewCoinCoord(toAddCoin);
		}
		
		for(Ball c : toRemoveCoin) {this.Coins.remove(c);}
		for(Ball c : toAddCoin) {this.Coins.add(c);}
		for(Ball b : toRemoveBall) {this.Balls.remove(b);}
		
		if(flag)
			corechat.sendNewBallCoord(getMBall());
	}
	
	private void mainBallReincarnate(Ball mb) {
		coordinates coord = randomGenerateCoordinates();
		mb.setX(coord.x);
		mb.setY(coord.y);
		mb.setR(BALL_RADIUS);
		mb.resetCoinCurrent();
		mb.setColor(MAIN_BALL_COLOR);
		this.corechat.sendBallResetPos(mb);
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
			if(!ball.getName().equals(corechat.userName))
				{corechat.sendHostExit(ball.getName());
				break;
				}
	}
}
