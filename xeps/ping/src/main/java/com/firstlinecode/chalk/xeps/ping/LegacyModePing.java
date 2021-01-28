package com.firstlinecode.chalk.xeps.ping;

import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.error.RemoteServerTimeout;
import com.firstlinecode.basalt.protocol.core.stanza.error.StanzaError;
import com.firstlinecode.chalk.IChatServices;
import com.firstlinecode.chalk.ISyncIqOperation;
import com.firstlinecode.chalk.IUnidirectionalStream;
import com.firstlinecode.chalk.SyncOperationTemplate;
import com.firstlinecode.chalk.core.ErrorException;
import com.firstlinecode.basalt.xeps.ping.Ping;

public class LegacyModePing implements IPing {
	private IChatServices chatServices;
	private String id;
	private int timeout;
	
	public LegacyModePing(IChatServices chatServices, int timeout) {
		this.chatServices = chatServices;
		this.timeout = timeout;
	}

	@Override
	public IPing.Result ping() {
		SyncOperationTemplate<Iq, IPing.Result> template = new SyncOperationTemplate<>(chatServices);
		
		try {
			return template.execute(new ISyncIqOperation<IPing.Result>() {

				@Override
				public void trigger(IUnidirectionalStream<Iq> stream) {
					Iq iq = new Iq(Iq.Type.SET);
					iq.setObject(new Ping());
					id = iq.getId();
					
					stream.send(iq, timeout);
				}

				@Override
				public boolean isErrorOccurred(StanzaError error) {
					if (id.equals(error.getId()))
						return true;
					
					return false;
				}

				@Override
				public boolean isResultReceived(Iq iq) {
					if (id.equals(iq.getId()))
						return true;
					
					return false;
				}

				@Override
				public Result processResult(Iq iq) {
					return IPing.Result.PONG;
				}
			});
		} catch (ErrorException e) {
			if (e.getError().getDefinedCondition().equals(RemoteServerTimeout.DEFINED_CONDITION)) {
				return IPing.Result.TIME_OUT;
			} else {
				return IPing.Result.SERVICE_UNAVAILABLE;
			}
		}
	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

}
