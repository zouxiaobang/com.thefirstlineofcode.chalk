package com.thefirstlineofcode.chalk.xeps.address;

import java.util.Properties;

import com.thefirstlineofcode.basalt.oxm.convention.NamingConventionParserFactory;
import com.thefirstlineofcode.basalt.oxm.convention.NamingConventionTranslatorFactory;
import com.thefirstlineofcode.basalt.protocol.core.MessageProtocolChain;
import com.thefirstlineofcode.basalt.xeps.address.Addresses;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;

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
