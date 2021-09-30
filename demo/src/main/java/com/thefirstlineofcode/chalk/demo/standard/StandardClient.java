package com.thefirstlineofcode.chalk.demo.standard;

import java.util.Properties;

import com.thefirstlineofcode.chalk.demo.Client;
import com.thefirstlineofcode.chalk.demo.Demo;
import com.thefirstlineofcode.chalk.leps.im.IInstantingMessager2;
import com.thefirstlineofcode.chalk.leps.im.InstantingMessagerPlugin2;
import com.thefirstlineofcode.chalk.xeps.ibr.IbrPlugin;
import com.thefirstlineofcode.chalk.xeps.muc.MucPlugin;

public abstract class StandardClient extends Client {

	public StandardClient(Demo demo, String clientName) {
		super(demo, clientName);
	}

	@Override
	protected void registerPlugins() {
		chatClient.register(IbrPlugin.class);
		
		Properties properties = new Properties();
		properties.put("subscriptionProtocol", IInstantingMessager2.Protocol.STANDARD);
		properties.put("messageProtocol", IInstantingMessager2.Protocol.STANDARD);
		chatClient.register(InstantingMessagerPlugin2.class, properties);
		
		chatClient.register(MucPlugin.class);
	}

}
