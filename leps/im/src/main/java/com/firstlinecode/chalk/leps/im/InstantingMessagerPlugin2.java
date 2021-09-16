package com.firstlinecode.chalk.leps.im;

import java.util.Properties;

import com.firstlinecode.basalt.leps.im.message.traceable.MessageRead;
import com.firstlinecode.basalt.leps.im.message.traceable.Trace;
import com.firstlinecode.basalt.leps.im.subscription.Subscribe;
import com.firstlinecode.basalt.leps.im.subscription.Subscribed;
import com.firstlinecode.basalt.leps.im.subscription.Unsubscribe;
import com.firstlinecode.basalt.leps.im.subscription.Unsubscribed;
import com.firstlinecode.basalt.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.basalt.oxm.parsers.SimpleObjectParserFactory;
import com.firstlinecode.basalt.protocol.core.IqProtocolChain;
import com.firstlinecode.basalt.protocol.core.MessageProtocolChain;
import com.firstlinecode.chalk.core.IChatSystem;
import com.firstlinecode.chalk.core.IPlugin;
import com.firstlinecode.chalk.im.InstantingMessagerPlugin;
import com.firstlinecode.chalk.xeps.delay.DelayPlugin;

public class InstantingMessagerPlugin2 implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(DelayPlugin.class);
		chatSystem.register(InstantingMessagerPlugin.class);
		
		chatSystem.registerParser(
				new IqProtocolChain(Subscribe.PROTOCOL),
				new NamingConventionParserFactory<>(
						Subscribe.class
				)
		);
		chatSystem.registerParser(
				new IqProtocolChain(Subscribed.PROTOCOL),
				new SimpleObjectParserFactory<>(
						Subscribed.PROTOCOL,
						Subscribed.class
				)
		);
		chatSystem.registerParser(
				new IqProtocolChain(Unsubscribe.PROTOCOL),
				new NamingConventionParserFactory<>(
						Unsubscribe.class
				)
		);
		chatSystem.registerParser(
				new IqProtocolChain(Unsubscribed.PROTOCOL),
				new NamingConventionParserFactory<>(
						Unsubscribed.class
				)
		);
		chatSystem.registerParser(
				new MessageProtocolChain(Trace.PROTOCOL),
				new NamingConventionParserFactory<>(
						Trace.class
				)
		);
		chatSystem.registerParser(
				new IqProtocolChain(Trace.PROTOCOL),
				new NamingConventionParserFactory<>(
						Trace.class
						)
				);
		chatSystem.registerParser(
				new IqProtocolChain(MessageRead.PROTOCOL),
				new NamingConventionParserFactory<>(
						MessageRead.class
						)
				);
		
		chatSystem.registerTranslator(
				Subscribe.class,
				new NamingConventionTranslatorFactory<>(
						Subscribe.class
				)
		);
		
		chatSystem.registerTranslator(
				Subscribed.class,
				new NamingConventionTranslatorFactory<>(
						Subscribed.class
				)
		);
		chatSystem.registerTranslator(
				Unsubscribe.class,
				new NamingConventionTranslatorFactory<>(
						Unsubscribe.class
				)
		);
		chatSystem.registerTranslator(
				Unsubscribed.class,
				new NamingConventionTranslatorFactory<>(
						Unsubscribed.class
				)
		);
		chatSystem.registerTranslator(
				Trace.class,
				new NamingConventionTranslatorFactory<>(
						Trace.class
				)
		);
		chatSystem.registerTranslator(
				MessageRead.class,
				new NamingConventionTranslatorFactory<>(
						MessageRead.class
						)
				);
		
		chatSystem.registerApi(IInstantingMessager2.class, InstantingMessager2.class, properties);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IInstantingMessager2.class);
		
		chatSystem.unregisterTranslator(Trace.class);
		chatSystem.unregisterTranslator(MessageRead.class);
		chatSystem.unregisterTranslator(Unsubscribed.class);
		chatSystem.unregisterTranslator(Unsubscribe.class);
		chatSystem.unregisterTranslator(Subscribed.class);
		chatSystem.unregisterTranslator(Subscribe.class);
		
		chatSystem.unregisterParser(
				new MessageProtocolChain(Trace.PROTOCOL));
		chatSystem.unregisterParser(
				new MessageProtocolChain(Trace.PROTOCOL));
		chatSystem.unregisterParser(
				new IqProtocolChain(MessageRead.PROTOCOL));
		chatSystem.unregisterParser(
				new IqProtocolChain(Unsubscribed.PROTOCOL));
		chatSystem.unregisterParser(
				new IqProtocolChain(Unsubscribe.PROTOCOL));
		chatSystem.unregisterParser(
				new IqProtocolChain(Subscribed.PROTOCOL));
		chatSystem.unregisterParser(
				new IqProtocolChain(Subscribe.PROTOCOL));
		
		chatSystem.unregister(InstantingMessagerPlugin.class);
		chatSystem.unregister(DelayPlugin.class);
	}

}
