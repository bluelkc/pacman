package drawtogether;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * BallController class implements a KeyListener to react on direction keys pressed.
 * BallController controls which direction and how fast a ball object moves.
 */
public class BallController implements KeyListener {
	
	/**
	 * The lowest speed a ball object should move.
	 */
	private static final int BASE_SPEED = 4; 
	
	/**
	 * The ball object the ball controller is controlling.
	 */
	private Ball ball;
	
	/**
	 * The offset in horizontal and vertical coordinates.
	 */
	private int offset;
		
	/**
	 * The JFrame the game is running in which provides the width and height of the window.
	 */
	private DrawFrame dframe;
	
	/**
	 * Class constructor.
	 * 
	 * @param ball  the ball the ball controller is controlling.
	 * @param dframe  the JFrame the game is running in.
	 */
	public BallController(Ball ball, DrawFrame dframe) {
		this.ball = ball;
		this.offset = BASE_SPEED;
		this.dframe = dframe;
	}
	
	/**
	 * The offset setter.
	 * 
	 * @param offset  offset in horizontal and vertical coordinates
	 */
	public void SetOffset(int offset) {
		this.offset = offset;
	}
	
	/**
	 * The ball setter.
	 * 
	 * @param ball  the ball object the ball controller is controlling
	 */
	public void setBall(Ball ball) {
		this.ball = ball;
	}
	
	/**
	 * Adjust the speed of the ball object based on its radius.
	 * 
	 * @param baseRadius  the smallest radius a ball object can have
	 */
	public void adjustSpeed(int baseRadius) {
		this.offset = BASE_SPEED - (this.ball.getR() - baseRadius)/6;
		this.offset = this.offset > 0 ? this.offset : 1;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(!ball.isLocal())
			return;
		
		int code = e.getKeyCode();
		switch(code) {
			case(KeyEvent.VK_UP):
				if((this.ball.getY() - this.ball.getR()) > 0) {
					this.ball.moveY(-offset);
				}
				else {
					this.ball.moveY(0);
				}
				break;
			case(KeyEvent.VK_DOWN):
				if((this.ball.getY() + this.ball.getR()) < this.dframe.getHeight()) {
					this.ball.moveY(offset);
				}
				else {
					this.ball.moveY(0);
				}
				break;
			case(KeyEvent.VK_LEFT):
				if((this.ball.getX() - this.ball.getR()) > 0) {
					this.ball.moveX(-offset);
				}
				else { 
					this.ball.moveX(0);
				}
				break;
			case(KeyEvent.VK_RIGHT):
				if((this.ball.getX() + this.ball.getR()) < this.dframe.getWidth()) {
					this.ball.moveX(offset);
				}
				else { 
					this.ball.moveX(0);
				}
				break;
			default:
		}
	}
	
	@Override 
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(!ball.isLocal())
			return;
		
		int code = e.getKeyCode();
		switch(code) {
			case(KeyEvent.VK_UP):
				this.ball.moveY(0);
				break;
			case(KeyEvent.VK_DOWN):
				this.ball.moveY(0);
				break;
			case(KeyEvent.VK_LEFT):
				this.ball.moveX(0);
				break;
			case(KeyEvent.VK_RIGHT):
				this.ball.moveX(0);
				break;
			default:
		}
	}
}