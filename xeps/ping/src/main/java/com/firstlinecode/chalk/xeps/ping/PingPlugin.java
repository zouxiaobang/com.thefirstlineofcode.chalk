package com.firstlinecode.chalk.xeps.ping;

import java.util.Properties;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.oxm.parsers.SimpleObjectParserFactory;
import com.firstlinecode.basalt.oxm.translators.SimpleObjectTranslatorFactory;
import com.firstlinecode.basalt.xeps.ping.Ping;
import com.firstlinecode.chalk.core.IChatSystem;
import com.firstlinecode.chalk.core.IPlugin;

public class PingPlugin implements IPlugin {
	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).next(Ping.PROTOCOL),
				new SimpleObjectParserFactory<>(Ping.PROTOCOL, Ping.class));
		chatSystem.registerTranslator(
				Ping.class,
				new SimpleObjectTranslatorFactory<>(Ping.class, Ping.PROTOCOL));
		
		chatSystem.registerApi(IPing.class, PingImpl.class, properties);			
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IPing.class);
		chatSystem.unregisterTranslator(Ping.class);
		chatSystem.unregisterParser(ProtocolChain.first(Iq.PROTOCOL).next(Ping.PROTOCOL));
	}

}
