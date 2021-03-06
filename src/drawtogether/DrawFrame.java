package drawtogether;

import java.awt.BorderLayout;  
import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;

/**
 * DrawFram class extends JFrame to provide the game window.
 */
public class DrawFrame extends JFrame{  
  
    private static final long serialVersionUID = 1L;
    public GameConnector gamecon;
    public GamePanel gpanel;
    
    /**
     * Class constructor.
     */
    public DrawFrame(){ 	
    	gamecon = new GameConnector();
    }
  
    /**
     * Initialize game window attributes.
     * Add GamePanel that renders game visualization objects.
     * Add GameConnector that communicates through uMundo channel.
     * Add Listener for window close event.
     */
    public void init() {  
        this.setTitle("Pac-Man");  
        this.setSize(new Dimension(700, 500));  
        this.setLocationRelativeTo(null);  
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        this.setVisible(true);         
        GamePanel gpanel = new GamePanel(this.gamecon, this);
        this.add(gpanel, BorderLayout.CENTER);    
        this.gpanel = gpanel;
        this.gamecon.gpanel = gpanel;
        
        this.addWindowListener( new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            	gpanel.GetGameLogic().sendHostExit();
            	System.exit(0);
            }
        });
    }   
    
    public static void main(String[] args){      	
    	EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
               DrawFrame drawFrame = new DrawFrame();
               drawFrame.init();
            }
        });
    }
}  