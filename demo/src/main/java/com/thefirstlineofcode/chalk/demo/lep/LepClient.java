package com.thefirstlineofcode.chalk.demo.lep;

import com.thefirstlineofcode.chalk.demo.Client;
import com.thefirstlineofcode.chalk.demo.Demo;
import com.thefirstlineofcode.chalk.leps.im.InstantingMessagerPlugin2;
import com.thefirstlineofcode.chalk.xeps.ibr.IbrPlugin;
import com.thefirstlineofcode.chalk.xeps.muc.MucPlugin;

public abstract class LepClient extends Client {

	public LepClient(Demo demo, String clientName) {
		super(demo, clientName);
	}

	protected void registerPlugins() {
		chatClient.register(IbrPlugin.class);
		chatClient.register(InstantingMessagerPlugin2.class);
		chatClient.register(MucPlugin.class);
	}

}
