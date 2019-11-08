package com.firstlinecode.chalk.xeps.delay;

import java.util.Properties;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.protocol.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.chalk.IChatSystem;
import com.firstlinecode.chalk.IPlugin;
import com.firstlinecode.basalt.xeps.delay.Delay;

public class DelayPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerParser(
				ProtocolChain.first(Message.PROTOCOL).
				next(Delay.PROTOCOL),
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
				ProtocolChain.first(Message.PROTOCOL).
				next(Delay.PROTOCOL)
		);
	}

}
