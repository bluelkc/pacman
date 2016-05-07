package drawtogether;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class BallController implements KeyListener {
	
	private static final int BASE_SPEED = 4; 
	
	private Ball ball;
	private int offset;
	private DrawFrame dframe;
	
	public BallController(Ball ball, DrawFrame dframe) {
		this.ball = ball;
		this.offset = BASE_SPEED;
		this.dframe = dframe;
	}
	
	public void SetOffset(int offset) {
		this.offset = offset;
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
	
	public void setBall(Ball ball) {
		this.ball = ball;
	}
	
	public void adjustSpeed(int baseRadius) {
		this.offset = BASE_SPEED - (this.ball.getR() - baseRadius)/6;
		this.offset = this.offset > 0 ? this.offset : 1;
	}
}
