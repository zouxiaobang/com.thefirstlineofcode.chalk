package com.firstlinecode.chalk;

import java.util.Properties;

import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.oxm.parsing.IParserFactory;
import com.firstlinecode.basalt.protocol.oxm.translating.ITranslatorFactory;
import com.firstlinecode.chalk.core.stream.StreamConfig;

public interface IChatSystem {
	void register(Class<? extends IPlugin> pluginClass);
	void unregister(Class<? extends IPlugin> pluginClass);
	
	void registerParser(ProtocolChain protocolChain, IParserFactory<?> parserFactory);
	void unregisterParser(ProtocolChain protocolChain);
	
	<T> void registerTranslator(Class<T> type, ITranslatorFactory<T> translatorFactory);
	void unregisterTranslator(Class<?> type);
	
	void registerApi(Class<?> apiType);
	void registerApi(Class<?> apiType, Properties properties);
	void registerApi(Class<?> apiType, boolean singleton);
	void registerApi(Class<?> apiType, Properties properties, boolean singleton);
	<T> void registerApi(Class<T> apiType, Class<? extends T> apiImplType);
	<T> void registerApi(Class<T> apiType, Class<? extends T> apiImplType, Properties properties);
	<T> void registerApi(Class<T> apiType, Class<? extends T> apiImplType, boolean singleton);
	<T> void registerApi(Class<T> apiType, Class<? extends T> apiImplType, Properties properties, boolean singleton);
	void unregisterApi(Class<?> apiType);
	
	StreamConfig getStreamConfig();
}
