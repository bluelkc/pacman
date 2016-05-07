package drawtogether;

import java.awt.BorderLayout;  
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;

public class DrawFrame extends JFrame{  
  
    private static final long serialVersionUID = 1L;
    public CoreChat corechat;
    public GamePanel gpanel;
    
    public DrawFrame(){ 	
    	corechat = new CoreChat();
    }
  
    public void init() {  

        this.setTitle("Pac-Man");  
        this.setSize(new Dimension(700, 500));  
        this.setLocationRelativeTo(null);  
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        this.setVisible(true);  
        
        GamePanel gpanel = new GamePanel(this.corechat, this);
        this.add(gpanel, BorderLayout.CENTER);    
        this.gpanel = gpanel;
        this.corechat.gpanel = gpanel;
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