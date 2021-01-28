package com.firstlinecode.chalk.core.stream.negotiants;

import java.util.List;

import com.firstlinecode.basalt.protocol.Constants;
import com.firstlinecode.basalt.protocol.core.IError;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stream.Feature;
import com.firstlinecode.basalt.protocol.core.stream.Features;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.protocol.core.stream.sasl.Mechanisms;
import com.firstlinecode.basalt.protocol.core.stream.tls.StartTls;
import com.firstlinecode.basalt.oxm.IOxmFactory;
import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.annotation.AnnotatedParserFactory;
import com.firstlinecode.basalt.oxm.parsers.core.stream.FeaturesParser;
import com.firstlinecode.basalt.oxm.parsers.core.stream.sasl.MechanismsParser;
import com.firstlinecode.basalt.oxm.parsers.core.stream.tls.StartTlsParser;
import com.firstlinecode.chalk.core.stream.INegotiationContext;
import com.firstlinecode.chalk.core.stream.NegotiationException;
import com.firstlinecode.chalk.core.stream.StandardStreamer;
import com.firstlinecode.chalk.network.ConnectionException;

public class InitialStreamNegotiant extends AbstractStreamNegotiant {
	protected static IOxmFactory oxmFactory = OxmService.createStreamOxmFactory();
	
	static {
		oxmFactory.register(ProtocolChain.first(Features.PROTOCOL),
				new AnnotatedParserFactory<>(FeaturesParser.class));
		oxmFactory.register(ProtocolChain.first(Features.PROTOCOL).next(StartTls.PROTOCOL),
				new AnnotatedParserFactory<>(StartTlsParser.class));
		oxmFactory.register(ProtocolChain.first(Features.PROTOCOL).next(Mechanisms.PROTOCOL),
				new AnnotatedParserFactory<>(MechanismsParser.class));
	}
	
	protected String hostName;
	protected String lang;
	
	public InitialStreamNegotiant(String hostName, String lang) {
		this.hostName = hostName;
		this.lang = lang;
	}

	@Override
	protected void doNegotiate(INegotiationContext context) throws ConnectionException, NegotiationException {
		Stream openStream = new Stream();
		openStream.setDefaultNamespace(Constants.C2S_DEFAULT_NAMESPACE);
		openStream.setTo(JabberId.parse(hostName));
		openStream.setLang(lang);
		openStream.setVersion(Constants.SPECIFICATION_VERSION);
		
		context.write(oxmFactory.translate(openStream));
		
		openStream = (Stream)oxmFactory.parse(readResponse());
		
		Object response = oxmFactory.parse(readResponse());
		if (response instanceof Features) {
			List<Feature> features = ((Features)response).getFeatures();
			context.setAttribute(StandardStreamer.NEGOTIATION_KEY_FEATURES, features);
		} else {
			processError((IError)response, context, oxmFactory);
		}

	}

}
