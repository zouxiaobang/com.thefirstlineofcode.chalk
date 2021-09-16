package com.firstlinecode.chalk.xeps.address;

import java.util.Properties;

import com.firstlinecode.basalt.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.basalt.protocol.core.MessageProtocolChain;
import com.firstlinecode.basalt.xeps.address.Addresses;
import com.firstlinecode.chalk.core.IChatSystem;
import com.firstlinecode.chalk.core.IPlugin;

public class AddressPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerParser(
				new MessageProtocolChain(Addresses.PROTOCOL),
				new NamingConventionParserFactory<>(
						Addresses.class
				)
		);
		
		chatSystem.registerTranslator(
				Addresses.class,
				new NamingConventionTranslatorFactory<>(
						Addresses.class
				)
		);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterTranslator(Addresses.class);
		
		chatSystem.unregisterParser(
				new MessageProtocolChain(Addresses.PROTOCOL)
		);
	}

}
