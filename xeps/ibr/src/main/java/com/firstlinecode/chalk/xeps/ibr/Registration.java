package com.firstlinecode.chalk.xeps.ibr;

import java.util.ArrayList;
import java.util.List;

import com.firstlinecode.basalt.protocol.core.IError;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stanza.error.Conflict;
import com.firstlinecode.basalt.protocol.core.stanza.error.NotAcceptable;
import com.firstlinecode.basalt.protocol.core.stanza.error.RemoteServerTimeout;
import com.firstlinecode.basalt.xeps.xdata.XData;
import com.firstlinecode.chalk.AuthFailureException;
import com.firstlinecode.chalk.IChatClient;
import com.firstlinecode.chalk.ISyncTask;
import com.firstlinecode.chalk.IUnidirectionalStream;
import com.firstlinecode.chalk.core.ErrorException;
import com.firstlinecode.chalk.core.stream.INegotiationListener;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.IConnectionListener;
import com.firstlinecode.basalt.xeps.ibr.IqRegister;
import com.firstlinecode.basalt.xeps.ibr.RegistrationForm;

public class Registration implements IRegistration {
	private StandardStreamConfig streamConfig;
	private List<IConnectionListener> connectionListeners = new ArrayList<>();
	private List<INegotiationListener> negotiationListeners = new ArrayList<>();
	
	@Override
	public void register(IRegistrationCallback callback) throws RegistrationException {
		IChatClient chatClient = new IbrChatClient(streamConfig);
		chatClient.register(InternalIbrPlugin.class);
		
		for (IConnectionListener connectionListener : connectionListeners) {
			chatClient.addConnectionListener(connectionListener);
		}
		
		for (INegotiationListener negotiationListener : negotiationListeners) {
			chatClient.addNegotiationListener(negotiationListener);
		}
		
		try {
			chatClient.connect(null);
		} catch (ConnectionException e) {
			if (!chatClient.isClosed())
				chatClient.close();
			
			throw new RegistrationException(IbrError.CONNECTION_ERROR, e);
		} catch (AuthFailureException e) {
			// it's impossible
		}
		
		
		try {
			Object filled = callback.fillOut(getRegistrationForm(chatClient));
			chatClient.getChatServices().getTaskService().execute(new RegisterTask(filled));
		} catch (ErrorException e) {
			IError error = e.getError();
			if (error.getDefinedCondition().equals(RemoteServerTimeout.DEFINED_CONDITION)) {
				throw new RegistrationException(IbrError.TIMEOUT);
			} else if (error.getDefinedCondition().equals(Conflict.DEFINED_CONDITION)) {
				throw new RegistrationException(IbrError.CONFLICT);
			} else if (error.getDefinedCondition().equals(NotAcceptable.DEFINED_CONDITION)) {
				throw new RegistrationException(IbrError.NOT_ACCEPTABLE);
			} else {
				throw new RegistrationException(IbrError.UNKNOWN, e);
			}
		} finally {
			if (!chatClient.isClosed())
				chatClient.close();
		}
	}
	
	private class RegisterTask implements ISyncTask<Iq, Void>  {
		private IqRegister iqRegister;
		
		public RegisterTask(Object filled) {
			iqRegister = new IqRegister();
			
			if (filled instanceof RegistrationForm) {
				iqRegister.setRegister(filled);
			} else if (filled instanceof XData) {
				iqRegister.setXData((XData)filled);
			} else {
				throw new IllegalArgumentException("Must be RegistrationForm or XData.");
			}
		}

		@Override
		public void trigger(IUnidirectionalStream<Iq> stream) {
			Iq iq = new Iq(iqRegister, Iq.Type.SET, Stanza.generateId("ibr"));
			iq.setObject(iqRegister);
			
			stream.send(iq);
		}

		@Override
		public Void processResult(Iq iq) {
			return null;
		}
		
	}
	
	private IqRegister getRegistrationForm(IChatClient chatClient) throws ErrorException {
		return chatClient.getChatServices().getTaskService().execute(new ISyncTask<Iq, IqRegister>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				Iq iq = new Iq(new IqRegister(), Iq.Type.GET, Stanza.generateId("ibr"));
				stream.send(iq);
			}

			@Override
			public IqRegister processResult(Iq iq) {
				return (IqRegister)iq.getObject();
			}
		});
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Feature not implemented.");
	}

	@Override
	public void addConnectionListener(IConnectionListener listener) {
		connectionListeners.add(listener);
	}

	@Override
	public void removeConnectionListener(IConnectionListener listener) {
		connectionListeners.remove(listener);
	}

	@Override
	public void addNegotiationListener(INegotiationListener listener) {
		negotiationListeners.add(listener);
	}

	@Override
	public void removeNegotiationListener(INegotiationListener listener) {
		negotiationListeners.remove(listener);
	}

}
