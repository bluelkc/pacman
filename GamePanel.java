package drawtogether;

import java.util.ArrayList;
import java.awt.Image;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener{  
  
	private static final long serialVersionUID = 1L;
    //private CoreChat corechat;
    private Timer timer;
    private GameLogic gl;

  
    public GamePanel(CoreChat corechat, DrawFrame dframe) {  

        //this.corechat = corechat;
        this.setBackground(Color.WHITE);
        this.setFocusable(true);
        this.requestFocus();
        
        this.gl = new GameLogic(this, dframe, corechat);      
        this.timer = new Timer(10, this);
        this.timer.start();
    }
    
    @Override 
    public void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	draw(g);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    	repaint();
    }
   
    public void draw(Graphics graphics) {
    	this.gl.Update();	
    	Graphics2D g2d = (Graphics2D) graphics;   	
    	ArrayList<Ball> balls = this.gl.getBalls();
    	ArrayList<Ball> coins = this.gl.getCoins();
	    
    	for(Ball b : balls) {
    		//drawBall(graphics, b.getX(), b.getY(), b.getR(), b.getColor());  
    		drawImage(graphics, b);
    	}
    	for(Ball c : coins) {
    		drawBall(graphics, c.getX(), c.getY(), c.getR(), c.getColor());   	
    	}
    	
    	int pos = 1;
    	g2d.setFont(new Font("Dialog", Font.BOLD, 14)); 
    	for(Ball b : this.gl.getBalls()) {
    		if(b.isLocal()) {
    			g2d.setColor(GameLogic.MAIN_BALL_COLOR);
    		} else {
    			g2d.setColor(GameLogic.OTHER_BALL_COLOR);
    		}
    		g2d.drawString(b.getName() + " : ", 80, 20 * pos);
    		g2d.drawString(Integer.toString(b.getScore()), 150, 20 * pos);
    		pos ++;
    	}
    }
    
    public void drawBall(Graphics g, int x, int y, int r, Color color) {
    	Graphics2D g2d = (Graphics2D) g;
    	g2d.setColor(color);
    	g2d.fillOval((int) (x - r), (int) (y - r),
	            (int)(2 * r), (int)(2 * r));
    }
    
    public void drawImage(Graphics g, Ball ball) {
    	Graphics2D g2d = (Graphics2D) g;
    	String direction = "right";
    	String version = ball.isLocal() ? "" : "-enemy";
    	switch(ball.getFace()) {
    	case TOP:
    		direction = "top";
    		break;
    	case TOPRIGHT:
    		direction = "topright";
    		break;
    	case RIGHT:
    		direction = "right";
    		break;
    	case BOTTOMRIGHT:
    		direction = "bottomright";
    		break;
    	case BOTTOM:
    		direction = "bottom";
    		break;
    	case BOTTOMLEFT:
    		direction = "bottomleft";
    		break;
    	case LEFT:
    		direction = "left";
    		break;
    	case TOPLEFT:
    		direction = "topleft";
    		break;
    		default:
    	}
    	int size = ball.getR() * 2;
    	ImageIcon ii = new ImageIcon("Images/pacman-" + Integer.toString(size) + "-" + direction + "-1" + version + ".png");
        Image image = ii.getImage();
    	g2d.drawImage(image, ball.getX() - ball.getR(), ball.getY() - ball.getR(), this); 
    }
    
    public GameLogic GetGameLogic() {
    	return gl;
    }
    
}  