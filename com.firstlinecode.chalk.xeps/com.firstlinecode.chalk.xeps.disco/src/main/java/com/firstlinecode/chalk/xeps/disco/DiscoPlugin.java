package com.firstlinecode.chalk.xeps.disco;

import java.util.Properties;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.protocol.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.basalt.xeps.xdata.XData;
import com.firstlinecode.chalk.IChatSystem;
import com.firstlinecode.chalk.IPlugin;
import com.firstlinecode.chalk.xeps.rsm.RsmPlugin;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.basalt.xeps.rsm.Set;

public class DiscoPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(RsmPlugin.class);
		
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(DiscoInfo.PROTOCOL),
				new NamingConventionParserFactory<>(
						DiscoInfo.class)
		);
		
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(DiscoInfo.PROTOCOL).
				next(XData.PROTOCOL),
				new NamingConventionParserFactory<>(
						XData.class)
		);
		
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(DiscoItems.PROTOCOL),
				new NamingConventionParserFactory<>(
						DiscoItems.class
				)
		);
		
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
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
				ProtocolChain.first(Iq.PROTOCOL).
				next(DiscoItems.PROTOCOL).
				next(Set.PROTOCOL)
		);
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(DiscoItems.PROTOCOL).
				next(XData.PROTOCOL)
		);
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(DiscoItems.PROTOCOL)
		);
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(DiscoInfo.PROTOCOL)
		);
		
		chatSystem.unregister(RsmPlugin.class);
	}

}
