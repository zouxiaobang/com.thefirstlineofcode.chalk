package com.thefirstlineofcode.chalk.xeps.delay;

import java.util.Properties;

import com.thefirstlineofcode.basalt.oxm.convention.NamingConventionParserFactory;
import com.thefirstlineofcode.basalt.oxm.convention.NamingConventionTranslatorFactory;
import com.thefirstlineofcode.basalt.xeps.delay.Delay;
import com.thefirstlineofcode.basalt.xmpp.core.MessageProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;

public class DelayPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerParser(
				new MessageProtocolChain(Delay.PROTOCOL),
				new NamingConventionParserFactory<>(
						Delay.class
				)
		);
		
		chatSystem.registerTranslator(
				Delay.class,
				new NamingConventionTranslatorFactory<>(
						Delay.class
				)
		);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterTranslator(Delay.class);
		
		chatSystem.unregisterParser(
				new MessageProtocolChain(Delay.PROTOCOL)
		);
	}

}
