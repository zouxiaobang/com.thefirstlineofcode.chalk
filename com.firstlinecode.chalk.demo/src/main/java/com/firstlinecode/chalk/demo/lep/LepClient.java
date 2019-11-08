package com.firstlinecode.chalk.demo.lep;

import com.firstlinecode.chalk.demo.Client;
import com.firstlinecode.chalk.demo.Demo;
import com.firstlinecode.chalk.leps.im.InstantingMessagerPlugin2;
import com.firstlinecode.chalk.xeps.ibr.IbrPlugin;
import com.firstlinecode.chalk.xeps.muc.MucPlugin;

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
