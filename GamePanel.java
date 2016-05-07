package drawtogether;

import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.umundo.core.Message;

public class GamePanel extends JPanel implements ActionListener{  
  
	private static final long serialVersionUID = 1L;
    private CoreChat corechat;
    private Timer timer;
    private GameLogic gl;

  
    public GamePanel(CoreChat corechat, DrawFrame dframe) {  

        this.corechat = corechat;
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
    	
    	//this.update(graphics);
    	this.gl.Update();
	
    	Graphics2D g2d = (Graphics2D) graphics;
    	
    	ArrayList<Ball> balls = this.gl.getBalls();
    	ArrayList<Ball> coins = this.gl.getCoins();
	    
    	for(Ball b : balls) {
    		drawBall(graphics, b.getX(), b.getY(), b.getR(), b.getColor());   
    	}
    	for(Ball c : coins) {
    		drawBall(graphics, c.getX(), c.getY(), c.getR(), c.getColor());   	
    	}
    	
    	int pos = 1;
    	for(Ball b : this.gl.getBalls()) {
    		if(b.isLocal()) {
    			g2d.setColor(GameLogic.MAIN_BALL_COLOR);
    		} else {
    			g2d.setColor(GameLogic.OTHER_BALL_COLOR);
    		}
    		g2d.drawString("coins", 100, 20 * pos);
    		g2d.drawString(Integer.toString(b.getScore()), 150, 20 * pos);
    		pos ++;
    	}
    	
    	g2d.setColor(Color.RED);
    	g2d.drawString("coins", 100, 20);
    	if(this.gl.getMBall() != null) {
    		g2d.drawString(Integer.toString(this.gl.getMBall().getScore()), 150, 20);
    	}
    }
    
    public void drawBall(Graphics g, int x, int y, int r, Color color) {
    	Graphics2D g2d = (Graphics2D) g;
    	g2d.setColor(color);
    	g2d.fillOval((int) (x - r), (int) (y - r),
	            (int)(2 * r), (int)(2 * r));
    }
    
    public GameLogic GetGameLogic() {
    	return gl;
    }
    
}  