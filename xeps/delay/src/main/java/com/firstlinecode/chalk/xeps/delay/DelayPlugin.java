package com.firstlinecode.chalk.xeps.delay;

import java.util.Properties;

import com.firstlinecode.basalt.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.basalt.protocol.core.MessageProtocolChain;
import com.firstlinecode.basalt.xeps.delay.Delay;
import com.firstlinecode.chalk.core.IChatSystem;
import com.firstlinecode.chalk.core.IPlugin;

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
