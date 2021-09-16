package com.firstlinecode.chalk.xeps.muc;

import java.util.Properties;

import com.firstlinecode.basalt.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.basalt.protocol.core.IqProtocolChain;
import com.firstlinecode.basalt.protocol.core.MessageProtocolChain;
import com.firstlinecode.basalt.protocol.core.PresenceProtocolChain;
import com.firstlinecode.basalt.xeps.muc.Muc;
import com.firstlinecode.basalt.xeps.muc.admin.MucAdmin;
import com.firstlinecode.basalt.xeps.muc.owner.MucOwner;
import com.firstlinecode.basalt.xeps.muc.user.MucUser;
import com.firstlinecode.basalt.xeps.muc.xconference.XConference;
import com.firstlinecode.basalt.xeps.xdata.XData;
import com.firstlinecode.chalk.core.IChatSystem;
import com.firstlinecode.chalk.core.IPlugin;
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
				new PresenceProtocolChain(Muc.PROTOCOL),
				new NamingConventionParserFactory<>(
						Muc.class
				)
		);
		
		chatSystem.registerParser(
				new PresenceProtocolChain(MucUser.PROTOCOL),
				new NamingConventionParserFactory<>(
						MucUser.class
				)
		);
		
		chatSystem.registerParser(
				new PresenceProtocolChain(MucUser.PROTOCOL),
				new NamingConventionParserFactory<>(
						MucUser.class
				)
		);
		
		chatSystem.registerParser(
				new IqProtocolChain(MucOwner.PROTOCOL),
				new NamingConventionParserFactory<>(
						MucOwner.class
				)
		);
		
		chatSystem.registerParser(
				new IqProtocolChain().
					next(MucOwner.PROTOCOL).
					next(XData.PROTOCOL),
				new NamingConventionParserFactory<>(
						XData.class
				)
		);
		
		chatSystem.registerParser(
				new MessageProtocolChain(XConference.PROTOCOL),
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
			new MessageProtocolChain(
					XConference.PROTOCOL));
		
		chatSystem.unregisterParser(
				new IqProtocolChain().
					next(MucOwner.PROTOCOL).
					next(XData.PROTOCOL));
		
		chatSystem.unregisterParser(
				new IqProtocolChain(MucOwner.PROTOCOL));
		
		chatSystem.unregisterParser(
				new MessageProtocolChain(MucUser.PROTOCOL));
		
		chatSystem.unregisterParser(
				new PresenceProtocolChain(MucUser.PROTOCOL));
		
		chatSystem.unregisterParser(
				new PresenceProtocolChain(Muc.PROTOCOL));
		
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
