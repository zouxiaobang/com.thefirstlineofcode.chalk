package com.firstlinecode.chalk.xeps.ping;

import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.error.RemoteServerTimeout;
import com.firstlinecode.chalk.IChatServices;
import com.firstlinecode.chalk.ISyncTask;
import com.firstlinecode.chalk.IUnidirectionalStream;
import com.firstlinecode.chalk.core.ErrorException;
import com.firstlinecode.basalt.xeps.ping.Ping;

public class TaskModePing implements IPing {
	private IChatServices chatServices;
	private int timeout;

	public TaskModePing(IChatServices chatServices, int timeout) {
		this.chatServices = chatServices;
		this.timeout = timeout;
	}

	@Override
	public Result ping() {
		try {
			return chatServices.getTaskService().execute(new ISyncTask<Iq, IPing.Result>() {

				@Override
				public void trigger(IUnidirectionalStream<Iq> stream) {
					Iq iq = new Iq(Iq.Type.SET);
					iq.setObject(new Ping());

					stream.send(iq, timeout);
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

