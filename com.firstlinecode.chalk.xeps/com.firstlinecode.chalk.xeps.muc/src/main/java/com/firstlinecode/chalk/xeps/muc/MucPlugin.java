package com.firstlinecode.chalk.xeps.muc;

import java.util.Properties;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.basalt.protocol.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.protocol.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.basalt.xeps.muc.Muc;
import com.firstlinecode.basalt.xeps.muc.admin.MucAdmin;
import com.firstlinecode.basalt.xeps.muc.owner.MucOwner;
import com.firstlinecode.basalt.xeps.muc.user.MucUser;
import com.firstlinecode.basalt.xeps.muc.xconference.XConference;
import com.firstlinecode.basalt.xeps.xdata.XData;
import com.firstlinecode.chalk.IChatSystem;
import com.firstlinecode.chalk.IPlugin;
import com.firstlinecode.chalk.xeps.address.AddressPlugin;
import com.firstlinecode.chalk.xeps.delay.DelayPlugin;
import com.firstlinecode.chalk.xeps.disco.DiscoPlugin;
import com.firstlinecode.chalk.xeps.xdata.XDataPlugin;

public class MucPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(DiscoPlugin.class);
		
		chatSystem.register(XDataPlugin.class);
		
		chatSystem.register(DelayPlugin.class);
		
		chatSystem.register(AddressPlugin.class);
		
		chatSystem.registerParser(
				ProtocolChain.first(Presence.PROTOCOL).
					next(Muc.PROTOCOL),
				new NamingConventionParserFactory<>(
						Muc.class
				)
		);
		
		chatSystem.registerParser(
				ProtocolChain.first(Presence.PROTOCOL).
					next(MucUser.PROTOCOL),
				new NamingConventionParserFactory<>(
						MucUser.class
				)
		);
		
		chatSystem.registerParser(
				ProtocolChain.first(Message.PROTOCOL).
					next(MucUser.PROTOCOL),
				new NamingConventionParserFactory<>(
						MucUser.class
				)
		);
		
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
					next(MucOwner.PROTOCOL),
				new NamingConventionParserFactory<>(
						MucOwner.class
				)
		);
		
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
					next(MucOwner.PROTOCOL).
					next(XData.PROTOCOL),
				new NamingConventionParserFactory<>(
						XData.class
				)
		);
		
		chatSystem.registerParser(
				ProtocolChain.first(Message.PROTOCOL).
					next(XConference.PROTOCOL),
				new NamingConventionParserFactory<>(
						XConference.class
				)
		);
		
		chatSystem.registerTranslator(
				Muc.class,
				new NamingConventionTranslatorFactory<>(
						Muc.class
				)
		);
		
		chatSystem.registerTranslator(
				MucUser.class,
				new NamingConventionTranslatorFactory<>(
						MucUser.class
				)
		);
		
		chatSystem.registerTranslator(
				MucAdmin.class,
				new NamingConventionTranslatorFactory<>(
						MucAdmin.class
				)
		);
		
		chatSystem.registerTranslator(
				MucOwner.class,
				new NamingConventionTranslatorFactory<>(
						MucOwner.class
				)
		);
		
		chatSystem.registerTranslator(
				XConference.class,
				new NamingConventionTranslatorFactory<>(
						XConference.class
				)
		);
		
		chatSystem.registerApi(IMucService.class, MucService.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IMucService.class);
		
		chatSystem.unregisterParser(
				ProtocolChain.first(Message.PROTOCOL).
					next(XConference.PROTOCOL));
		
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
					next(MucOwner.PROTOCOL).
					next(XData.PROTOCOL));
		
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
					next(MucOwner.PROTOCOL));
		
		chatSystem.unregisterParser(
				ProtocolChain.first(Message.PROTOCOL).
					next(MucUser.PROTOCOL));
		
		chatSystem.unregisterParser(
				ProtocolChain.first(Presence.PROTOCOL).
					next(MucUser.PROTOCOL));
		
		chatSystem.unregisterParser(
				ProtocolChain.first(Presence.PROTOCOL).
					next(Muc.PROTOCOL));
		
		chatSystem.unregisterTranslator(XConference.class);
		
		chatSystem.unregisterTranslator(MucOwner.class);
		
		chatSystem.unregisterTranslator(MucUser.class);
		
		chatSystem.unregisterTranslator(Muc.class);
		
		chatSystem.unregister(AddressPlugin.class);
		
		chatSystem.unregister(DelayPlugin.class);
		
		chatSystem.unregister(DiscoPlugin.class);
		
		chatSystem.unregister(XDataPlugin.class);
	}

}
