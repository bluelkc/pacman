package drawtogether;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
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

/**
 * GameConnector is the class that handles all traffic between nodes in a single uMundo channel.
 * GameConnector will process all incoming messages and call the core game logic to react accordingly.
 * GameConnector will also publish messages generated locally within the channel to sync among instances of games.
 */
public class GameConnector{	
	
	/**
	 * The flag indicating whether this instance of game acts as the main server.
	 */
	public boolean isHost = false;
	
	/**
	 * The JPanel where GameLogic resides in.
	 */
	public GamePanel gpanel;
	
	/**
	 * The user name of this host.
	 */
	public String userName;
	
	public Discovery disc;
	public Node gameNode;
	public Subscriber gameSub;
	public Publisher gamePub;	
	public HashMap<String, String> participants = new HashMap<String, String>();
	public BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * Class constructor.
	 */
	public GameConnector() {	
		disc = new Discovery(DiscoveryType.MDNS);	
		gameNode = new Node();
		gameSub = new Subscriber("pacman");
		gameSub.setReceiver(new GameReceiver());	
		gamePub = new Publisher("pacman");		
		disc.add(gameNode);
		
	    JFrame frame = new JFrame("Username");
	    userName = JOptionPane.showInputDialog(frame, "Please Input your Username");
    
		gamePub.setGreeter(new GameGreeter(userName));
		gameNode.addPublisher(gamePub);
		gameNode.addSubscriber(gameSub);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		
		if( gamePub.waitForSubscribers(0) == 0)
			isHost = true;
	}
	
	class GameReceiver extends Receiver {
		@Override
		public void receive(Message msg) {
			if (msg.getMeta().containsKey("participant")) {			
				GameConnector.this.participants.put(msg.getMeta("subscriber"), msg.getMeta("participant"));
				System.out.println(msg.getMeta("participant") + " joined the game");			
			} else {				
				try {
					handleCtrlMsg(msg);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}	
		}
	}

	class GameGreeter extends Greeter {
		public String userName;
		public GameGreeter(String userName) {
			this.userName = userName;
		}

		@Override
		public void welcome(Publisher pub, SubscriberStub subStub) {
			Message greeting = Message.toSubscriber(subStub.getUUID());
			greeting.putMeta("participant", userName);
			greeting.putMeta("subscriber", GameConnector.this.gameSub.getUUID());
			pub.send(greeting);	
		}

		@Override
		public void farewell(Publisher pub, SubscriberStub subStub) {
			if (GameConnector.this.participants.containsKey(subStub.getUUID())) {
				System.out.println(GameConnector.this.participants.get(subStub.getUUID()) + " left the game");
				gpanel.GetGameLogic().removeOtherBall(subStub.getUUID());
			} else {
				System.out.println("An unknown user left the game: " + subStub.getUUID());	
			}
		}	
	}
	
	/**
	 * Send a list of coin coordinates to the channel.
	 * 
	 * @param coins  the list of ball objects whose coordinates are to be sent.
	 */
	public void sendNewCoinCoord(ArrayList<Ball> coins) {		
		StringBuilder sb = new StringBuilder();	
		for(Ball coin : coins)
			sb.append(coin.getXY_Str_coin());	
		Message msg = new Message();
		msg.putMeta("userName", userName);
		msg.putMeta("ctrlMsg", sb.toString());
		gamePub.send(msg);
	}
	
	/**
	 * Send the coordinates of a ball to the channel.
	 * It asks for resetting its currently absorbed coins in addition.
	 * 
	 * @param ball  the ball object whose coordinates is to be sent.
	 */
	public void sendBallResetPos(Ball ball) {
		Message msg = new Message();
		msg.putMeta("userName", this.userName);
		msg.putMeta("resetPos", ball.getXY_Str());
		gamePub.send(msg);
	}
	
	/**
	 * Send the coordinates of a ball to the channel.
	 * 
	 * @param ball  the ball object whose coordinates is to be sent.
	 */
	public void sendNewBallCoord(Ball ball) {
		Message msg = new Message();
		msg.putMeta("userName", this.userName);
		msg.putMeta("ctrlMsg", ball.getXY_Str());
		gamePub.send(msg);
	}
	
	/**
	 * Send the score and current absorbed coins of a ball to the channel
	 * 
	 * @param ball  the ball object whose states are to be sent.
	 */
	public void sendNewBallScore(Ball ball) {
		Message msg = new Message();
		msg.putMeta("userName", ball.getName());
		msg.putMeta("score", Integer.toString(ball.getScore()));
		msg.putMeta("current", Integer.toString(ball.getCurrentCoins()));
		gamePub.send(msg);
	}
	
	/**
	 * Send a signal to the channel indicating the leaving of the sender.
	 * It also indicates the user name of the new appointed host.
	 *  
	 * @param nextHost the user name of the appointed host.
	 */
	public void sendHostExit(String nextHost) {
		if(isHost) {
			Message msg = new Message();
			msg.putMeta("userName", userName);
			msg.putMeta("ctrlMsg", "HostExit," + nextHost);
			gamePub.send(msg);
		}
	}
	
	/**
	 * Send a acknowledgement message indicating the readiness of the sender to receive messages.
	 */
	public void sendReadyToPlay() {
		Message msg = new Message();
		msg.putMeta("userName", this.userName);
		msg.putMeta("ready", "true");
		gamePub.send(msg);
	}
	
	/**
	 * Process received message and call corresponding game logic functions.
	 * 
	 * @param msg  the message that is received from the channel.
	 */
	private void handleCtrlMsg(Message msg) {
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
			gl.setOtherBallPosition(str, username);
		}
		
		String score = msg.getMeta("score");
		if(score != null && !score.isEmpty()) {
			int s = Integer.valueOf(score);
			String username = msg.getMeta("userName");	
			gl.setOtherBallScore(s, username);
		}
		String current_coins = msg.getMeta("current");
		if(current_coins != null && !current_coins.isEmpty()) {
			int s = Integer.valueOf(current_coins);
			String username = msg.getMeta("userName");
			gl.setOtherBallCurrentCoins(s, username);
		}
		
		String[] str = msg.getMeta("ctrlMsg").split(",");
		if(str[0].equals("Ball")) {			
			gl.updateOtherBall(str, msg.getMeta("userName"));
		}		
		if(str[0].equals("Coin")) {
			System.out.println("received coins");
			gl.updateCoins(str);
	    }				
		if(str[0].equals("HostExit")) {
			if(str[1].equals(userName)) {
				isHost=true;
			}
		}
	}
}