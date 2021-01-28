package com.firstlinecode.chalk.examples;

import com.firstlinecode.chalk.core.AuthFailureException;
import com.firstlinecode.chalk.core.IChatClient;
import com.firstlinecode.chalk.core.StandardChatClient;
import com.firstlinecode.chalk.core.stream.UsernamePasswordToken;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.xeps.ping.IPing;
import com.firstlinecode.chalk.xeps.ping.PingPlugin;
import com.mongodb.client.MongoDatabase;

public class PingExample extends AbstractExample {

	@Override
	protected void runExample() {
		IChatClient chatClient = new StandardChatClient(createStreamConfig());
		chatClient.register(PingPlugin.class);
		
		chatClient.addConnectionListener(this);
		try {
			chatClient.connect(new UsernamePasswordToken("dongger", "a_stupid_man"));
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (AuthFailureException e) {
			e.printStackTrace();
		}
		
		IPing ping = chatClient.createApi(IPing.class);
		ping.setTimeout(5 * 60 * 1000);
		
		IPing.Result result = ping.ping();
		if (result == IPing.Result.PONG) {
			System.out.println("Ping Result: Pong.");
		} else if (result == IPing.Result.SERVICE_UNAVAILABLE) {
			System.out.println("Ping Result: Service Unavailable.");
		} else {
			System.out.println("Ping Result: Timeout.");
		}
		
		chatClient.close();
	}

	@Override
	protected String[][] getUserNameAndPasswords() {
		return new String[][] {new String[] {"dongger", "a_stupid_man"}};
	}

	@Override
	protected void cleanExampleData(MongoDatabase database) {}
	
}
