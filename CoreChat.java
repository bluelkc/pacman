package drawtogether;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.umundo.core.Discovery;
import org.umundo.core.Discovery.DiscoveryType;
import org.umundo.core.Greeter;
import org.umundo.core.Message;
import org.umundo.core.Node;
import org.umundo.core.Publisher;
import org.umundo.core.Receiver;
import org.umundo.core.Subscriber;
import org.umundo.core.SubscriberStub;

import drawtogether.Ball.Facing;

/**
 * Make sure to set the correct path to umundo.jar in build.properties if you want to use ant!
 */

public class CoreChat{
	
	public boolean isHost = false;
	
	public GamePanel gpanel;
	public Discovery disc;
	public Node chatNode;
	public Subscriber chatSub;
	public Publisher chatPub;	
	public String userName;
	public Queue<Message> msgQueue = new LinkedList<Message>();
	public HashMap<String, String> participants = new HashMap<String, String>();
	public BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public CoreChat() {	
		disc = new Discovery(DiscoveryType.MDNS);	
		chatNode = new Node();
		chatSub = new Subscriber("coreChat");
		chatSub.setReceiver(new ChatReceiver());	
		chatPub = new Publisher("coreChat");		
		disc.add(chatNode);
		
	    JFrame frame = new JFrame("Username");
	    userName = JOptionPane.showInputDialog(frame, "Please Input your Username");
    
		chatPub.setGreeter(new ChatGreeter(userName));
		chatNode.addPublisher(chatPub);
		chatNode.addSubscriber(chatSub);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		
		if( chatPub.waitForSubscribers(0) == 0)
			isHost = true;
	}
	
	class ChatReceiver extends Receiver {

		@Override
		public void receive(Message msg) {
			if (msg.getMeta().containsKey("participant")) {			
				CoreChat.this.participants.put(msg.getMeta("subscriber"), msg.getMeta("participant"));
				System.out.println(msg.getMeta("participant") + " joined the chat");			
			} else {				
				try {
					GameLogic gl = gpanel.GetGameLogic();	
					String ready = msg.getMeta("ready");
					if(ready.equals("true")) {
						sendNewBallCoord(gl.getMBall());
						sendNewBallScore(gl.getMBall());
						if(isHost) {
							sendNewCoinCoord(gl.getCoins());
						}
					}	
					
					String resetPos = msg.getMeta("resetPos");
					if(resetPos != null && !resetPos.isEmpty()) {
						String[] str = resetPos.split(",");
						String username = msg.getMeta("userName");
						for (Ball ball : gl.getBalls()) 
							if(ball.getName().equals(username)) {
								ball.setX(Integer.valueOf(str[1]));
								ball.setY(Integer.valueOf(str[2]));
								ball.setColor(GameLogic.OTHER_BALL_COLOR);
								ball.setR(GameLogic.BALL_RADIUS);
								ball.resetCoinCurrent();
								break;
						}
					}
					
					String score = msg.getMeta("score");
					if(score != null && !score.isEmpty()) {
						int s = Integer.valueOf(score);
						String username = msg.getMeta("userName");
						for (Ball ball : gl.getBalls()) 
							if(ball.getName().equals(username)) {
								ball.setScore(s);
								break;
							}	
					}
					String current_coins = msg.getMeta("current");
					if(current_coins != null && !current_coins.isEmpty()) {
						int s = Integer.valueOf(current_coins);
						String username = msg.getMeta("userName");
						for (Ball ball : gl.getBalls()) 
							if(ball.getName().equals(username)) {
								ball.setCurrentCoins(s);;
								break;
							}	
					}
					
					String[] str = msg.getMeta("chatMsg").split(",");
					if(str[0].equals("Ball")) {			
						updateBall(str, msg.getMeta("userName"), gl);
					}		
					if(str[0].equals("Coin")) {
						updateCoins(str, gl);			
				    }				
					if(str[0].equals("HostExit"))
						if(str[1].equals(userName))
							isHost=true;
					
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}	
		}
		
		private void updateBall(String[] str, String username, GameLogic gl) {		
	    	boolean ball_existed = false;  	
	    	for (Ball ball : gl.getBalls()) 
	    		if(ball.getName().equals(username)) {
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
	    		gl.getBalls().add(ball);
	    	}
		}
		
		private void updateCoins(String[] str, GameLogic gl) {
	    	if(!isHost) {
	    		gl.getCoins().clear();
	    		for (int i = 0; i + 2 < str.length; i = i + 3) {
	    			Ball coin = new Ball(GameLogic.COIN_RADIUS, Integer.valueOf(str[i+1]), Integer.valueOf(str[i+2]),
	    					GameLogic.COIN_COLOR, userName, true);
	    			Random random = new Random();
					int color = random.nextInt(3) + i + 1;
		            coin.setScore(color);
	    			gl.getCoins().add(coin);
	    		}
	    	}
		}
	}

	class ChatGreeter extends Greeter {
		public String userName;
		public ChatGreeter(String userName) {
			this.userName = userName;
		}

		@Override
		public void welcome(Publisher pub, SubscriberStub subStub) {
			Message greeting = Message.toSubscriber(subStub.getUUID());
			greeting.putMeta("participant", userName);
			greeting.putMeta("subscriber", CoreChat.this.chatSub.getUUID());
			pub.send(greeting);	
		}

		@Override
		public void farewell(Publisher pub, SubscriberStub subStub) {
			if (CoreChat.this.participants.containsKey(subStub.getUUID())) {
				System.out.println(CoreChat.this.participants.get(subStub.getUUID()) + " left the chat");
				GameLogic gl = gpanel.GetGameLogic();
				Ball toRemove = null;
				for (Ball ball : gl.getBalls()) 
		    		if(ball.getName().equals(CoreChat.this.participants.get(subStub.getUUID()))) {
						toRemove = ball;
						break;
		    		}  	
				if(toRemove != null)
				gl.getBalls().remove(toRemove);
			} else {
				System.out.println("An unknown user left the chat: " + subStub.getUUID());	
			}
		}	
	}
	
	public void run() {
		System.out.println("Start typing messages (empty line to quit):");
		while (true) {
			String line = "";
			try {
				line = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (line.length() == 0)
				break;
			Message msg = new Message();
			msg.putMeta("userName", userName);
			msg.putMeta("chatMsg", line);
			chatPub.send(msg);
		}
		chatNode.removePublisher(chatPub);
		chatNode.removeSubscriber(chatSub);
	}
	
	public void sendNewCoinCoord(ArrayList<Ball> coins) {		
		StringBuilder sb = new StringBuilder();	
		for(Ball coin : coins)
			sb.append(coin.getXY_Str_coin());	
		Message msg = new Message();
		msg.putMeta("userName", userName);
		msg.putMeta("chatMsg", sb.toString());
		chatPub.send(msg);
	}
	
	public void sendBallResetPos(Ball ball) {
		Message msg = new Message();
		msg.putMeta("userName", this.userName);
		msg.putMeta("resetPos", ball.getXY_Str());
		chatPub.send(msg);
	}
	
	public void sendNewBallCoord(Ball ball) {
		Message msg = new Message();
		msg.putMeta("userName", this.userName);
		msg.putMeta("chatMsg", ball.getXY_Str());
		chatPub.send(msg);
	}
	
	public void sendNewBallScore(Ball ball) {
		Message msg = new Message();
		msg.putMeta("userName", ball.getName());
		msg.putMeta("score", Integer.toString(ball.getScore()));
		msg.putMeta("current", Integer.toString(ball.getCurrentCoins()));
		chatPub.send(msg);
	}
	
	public void sendHostExit(String nextHost) {
		if(isHost) {
			Message msg = new Message();
			msg.putMeta("userName", userName);
			msg.putMeta("chatMsg", "HostExit,"+nextHost);
			chatPub.send(msg);
		}
	}
	
	public void sendReadyToPlay() {
		Message msg = new Message();
		msg.putMeta("userName", this.userName);
		msg.putMeta("ready", "true");
		chatPub.send(msg);
	}
}