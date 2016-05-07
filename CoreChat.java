package drawtogether;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

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

/**
 * Make sure to set the correct path to umundo.jar in build.properties if you want to use ant!
 */

public class CoreChat{
	
	//public boolean isServer = true;
	
	public GamePanel gpanel;
	public Discovery disc;
	public Node chatNode;
	public Subscriber chatSub;
	public Publisher chatPub;
	
	public String userName;
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
	}
	
	class ChatReceiver extends Receiver {

		@Override
		public void receive(Message msg) {
			if (msg.getMeta().containsKey("participant")) {			
				CoreChat.this.participants.put(msg.getMeta("subscriber"), msg.getMeta("participant"));
				System.out.println(msg.getMeta("participant") + " joined the chat");		
//				try {
//					GameLogic gl = gpanel.GetGameLogic();
//					sendNewBallCoord(gl.getMBall());
//					sendNewCoinCoord(gl.getCoins());
//				} catch(Exception e) {
//					
//				}		
			} else {				
				//System.out.println(msg.getMeta("userName") + ": "
				//		+ msg.getMeta("chatMsg"));
				try {
					GameLogic gl = gpanel.GetGameLogic();	
					String ready = msg.getMeta("ready");
					if(ready.equals("true")) {
						sendNewBallCoord(gl.getMBall());
						if(userName.equals("host")) {
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
					
					String[] str = msg.getMeta("chatMsg").split(",");
					if(str[0].equals("Ball")) {			
						updateBall(str, msg.getMeta("userName"), gl);
					}		
					if(str[0].equals("Coin")) {
						updateCoins(str, gl);			
				    }
				} catch (Exception e) {
					System.out.println("receive " + e.getMessage());
				}
			}	
		}
		
		private void updateBall(String[] str, String username, GameLogic gl) {		
	    	boolean ball_existed = false;  	
	    	for (Ball ball : gl.getBalls()) 
	    		if(ball.getName().equals(username)) {
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
	    		if(ball.getName().equals("host")) {
	    			System.out.println("Received host msg");
	    		}
	    	}
		}
		
		private void updateCoins(String[] str, GameLogic gl) {
	    	if(!userName.equals("host")) {
	    		gl.getCoins().clear();
	    		for (int i = 0; i + 2 < str.length; i = i + 3) {
	    			Ball coin = new Ball(GameLogic.COIN_RADIUS, Integer.valueOf(str[i+1]), Integer.valueOf(str[i+2]),
	    					GameLogic.COIN_COLOR, userName, true);
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
	
	public void sendReadyToPlay() {
		Message msg = new Message();
		msg.putMeta("userName", this.userName);
		msg.putMeta("ready", "true");
		chatPub.send(msg);
	}
}