package com.firstlinecode.chalk.leps.im;

import java.util.Properties;

import com.firstlinecode.basalt.leps.im.message.traceable.MessageRead;
import com.firstlinecode.basalt.leps.im.message.traceable.Trace;
import com.firstlinecode.basalt.leps.im.subscription.Subscribe;
import com.firstlinecode.basalt.leps.im.subscription.Subscribed;
import com.firstlinecode.basalt.leps.im.subscription.Unsubscribe;
import com.firstlinecode.basalt.leps.im.subscription.Unsubscribed;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.basalt.oxm.parsers.SimpleObjectParserFactory;
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
				ProtocolChain.first(Iq.PROTOCOL).
				next(Subscribe.PROTOCOL),
				new NamingConventionParserFactory<>(
						Subscribe.class
				)
		);
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(Subscribed.PROTOCOL),
				new SimpleObjectParserFactory<>(
						Subscribed.PROTOCOL,
						Subscribed.class
				)
		);
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(Unsubscribe.PROTOCOL),
				new NamingConventionParserFactory<>(
						Unsubscribe.class
				)
		);
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(Unsubscribed.PROTOCOL),
				new NamingConventionParserFactory<>(
						Unsubscribed.class
				)
		);
		chatSystem.registerParser(
				ProtocolChain.first(Message.PROTOCOL).
				next(Trace.PROTOCOL),
				new NamingConventionParserFactory<>(
						Trace.class
				)
		);
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(Trace.PROTOCOL),
				new NamingConventionParserFactory<>(
						Trace.class
						)
				);
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(MessageRead.PROTOCOL),
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
				ProtocolChain.first(Message.PROTOCOL).
				next(Trace.PROTOCOL));
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(Trace.PROTOCOL));
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(MessageRead.PROTOCOL));
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(Unsubscribed.PROTOCOL));
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(Unsubscribe.PROTOCOL));
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(Subscribed.PROTOCOL));
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(Subscribe.PROTOCOL));
		
		chatSystem.unregister(InstantingMessagerPlugin.class);
		chatSystem.unregister(DelayPlugin.class);
	}

}
