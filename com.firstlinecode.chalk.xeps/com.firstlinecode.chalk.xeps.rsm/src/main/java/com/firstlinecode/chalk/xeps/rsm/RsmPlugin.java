package com.firstlinecode.chalk.xeps.rsm;

import java.util.Properties;

import com.firstlinecode.basalt.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.chalk.IChatSystem;
import com.firstlinecode.chalk.IPlugin;
import com.firstlinecode.basalt.xeps.rsm.Set;

public class RsmPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerTranslator(Set.class,
				new NamingConventionTranslatorFactory<>(
						Set.class
				)
		);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterTranslator(Set.class);
	}

}
