package com.firstlinecode.chalk.xeps.ibr;

import java.util.Properties;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.xeps.xdata.XData;
import com.firstlinecode.chalk.IChatSystem;
import com.firstlinecode.chalk.IPlugin;
import com.firstlinecode.chalk.xeps.oob.OobPlugin;
import com.firstlinecode.chalk.xeps.xdata.XDataPlugin;
import com.firstlinecode.basalt.xeps.ibr.IqRegister;
import com.firstlinecode.basalt.xeps.ibr.oxm.IqRegisterParserFactory;
import com.firstlinecode.basalt.xeps.ibr.oxm.IqRegisterTranslatorFactory;
import com.firstlinecode.basalt.xeps.oob.XOob;

public class InternalIbrPlugin implements IPlugin {
	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(OobPlugin.class);
		chatSystem.register(XDataPlugin.class);
		
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(IqRegister.PROTOCOL),
				new IqRegisterParserFactory()
		);
		
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(IqRegister.PROTOCOL).
				next(XData.PROTOCOL),
				new NamingConventionParserFactory<>(
						XData.class
				)
		);
		
		chatSystem.registerParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(IqRegister.PROTOCOL).
				next(XOob.PROTOCOL),
				new NamingConventionParserFactory<>(
						XOob.class
				)
		);
		
		chatSystem.registerTranslator(
				IqRegister.class,
				new IqRegisterTranslatorFactory()
		);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterTranslator(IqRegister.class);
		
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(IqRegister.PROTOCOL)
		);
		
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(IqRegister.PROTOCOL).
				next(XData.PROTOCOL)
		);
		
		chatSystem.unregisterParser(
				ProtocolChain.first(Iq.PROTOCOL).
				next(IqRegister.PROTOCOL).
				next(XOob.PROTOCOL)
		);
		
		chatSystem.unregister(XDataPlugin.class);
		chatSystem.unregister(OobPlugin.class);
	}
}
