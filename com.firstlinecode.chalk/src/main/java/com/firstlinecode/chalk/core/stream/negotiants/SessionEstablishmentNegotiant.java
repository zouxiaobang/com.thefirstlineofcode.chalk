package com.firstlinecode.chalk.core.stream.negotiants;

import java.util.List;

import com.firstlinecode.basalt.protocol.core.IError;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stream.Feature;
import com.firstlinecode.basalt.protocol.core.stream.Session;
import com.firstlinecode.basalt.oxm.IOxmFactory;
import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.translators.SimpleObjectTranslatorFactory;
import com.firstlinecode.chalk.core.stream.INegotiationContext;
import com.firstlinecode.chalk.core.stream.NegotiationException;
import com.firstlinecode.chalk.core.stream.StandardStreamer;
import com.firstlinecode.chalk.network.ConnectionException;

public class SessionEstablishmentNegotiant extends AbstractStreamNegotiant {
	private static IOxmFactory oxmFactory = OxmService.createMinimumOxmFactory();
	
	static {
		oxmFactory.register(Session.class,
				new SimpleObjectTranslatorFactory<>(
						Session.class,
						Session.PROTOCOL
				)
		);
	}
	
	@Override
	protected void doNegotiate(INegotiationContext context) throws ConnectionException, NegotiationException {
		@SuppressWarnings("unchecked")
		List<Feature> features = (List<Feature>)context.getAttribute(
				StandardStreamer.NEGOTIATION_KEY_FEATURES);
		Session session = findSession(features);
		
		if (session != null) {
			negotiateSession(context);
		}
		
		
	}

	private void negotiateSession(INegotiationContext context) throws ConnectionException, NegotiationException {
		Iq iq = new Iq(Iq.Type.SET);
		iq.setObject(new Session());
		
		context.write(oxmFactory.translate(iq));
		
		Object response = oxmFactory.parse(readResponse());
		
		if (response instanceof Iq) {
			iq = (Iq)response;
			if (Iq.Type.RESULT != iq.getType()) {
				throw new NegotiationException(this);
			}
		} else {
			processError((IError)response, context, oxmFactory);
		}
	}

	private Session findSession(List<Feature> features) {
		for (Feature feature : features) {
			if (feature instanceof Session)
				return (Session)feature;
		}
		
		return null;
	}

}
