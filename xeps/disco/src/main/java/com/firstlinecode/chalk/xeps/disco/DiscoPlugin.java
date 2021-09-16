package com.firstlinecode.chalk.xeps.disco;

import java.util.Properties;

import com.firstlinecode.basalt.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.basalt.protocol.core.IqProtocolChain;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.basalt.xeps.rsm.Set;
import com.firstlinecode.basalt.xeps.xdata.XData;
import com.firstlinecode.chalk.core.IChatSystem;
import com.firstlinecode.chalk.core.IPlugin;
import com.firstlinecode.chalk.xeps.rsm.RsmPlugin;

public class DiscoPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(RsmPlugin.class);
		
		chatSystem.registerParser(
				new IqProtocolChain(DiscoInfo.PROTOCOL),
				new NamingConventionParserFactory<>(
						DiscoInfo.class)
		);
		
		chatSystem.registerParser(
				new IqProtocolChain().
				next(DiscoInfo.PROTOCOL).
				next(XData.PROTOCOL),
				new NamingConventionParserFactory<>(
						XData.class)
		);
		
		chatSystem.registerParser(
				new IqProtocolChain(DiscoItems.PROTOCOL),
				new NamingConventionParserFactory<>(
						DiscoItems.class
				)
		);
		
		chatSystem.registerParser(
				new IqProtocolChain().
					next(DiscoItems.PROTOCOL).
					next(Set.PROTOCOL),
					new NamingConventionParserFactory<>(
							Set.class
					)
				);
		
		chatSystem.registerTranslator(
				DiscoInfo.class,
				new NamingConventionTranslatorFactory<>(
						DiscoInfo.class
				)
		);
		
		chatSystem.registerTranslator(
				DiscoItems.class,
				new NamingConventionTranslatorFactory<>(
						DiscoItems.class
				)
		);
		
		chatSystem.registerApi(IServiceDiscovery.class, ServiceDiscovery.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IServiceDiscovery.class);
		
		chatSystem.unregisterTranslator(DiscoItems.class);
		chatSystem.unregisterTranslator(DiscoInfo.class);
		
		chatSystem.unregisterParser(
				new IqProtocolChain().
				next(DiscoItems.PROTOCOL).
				next(Set.PROTOCOL)
		);
		chatSystem.unregisterParser(
				new IqProtocolChain().
				next(DiscoItems.PROTOCOL).
				next(XData.PROTOCOL)
		);
		chatSystem.unregisterParser(
				new IqProtocolChain(DiscoItems.PROTOCOL)
		);
		chatSystem.unregisterParser(
				new IqProtocolChain(DiscoInfo.PROTOCOL)
		);
		
		chatSystem.unregister(RsmPlugin.class);
	}

}
