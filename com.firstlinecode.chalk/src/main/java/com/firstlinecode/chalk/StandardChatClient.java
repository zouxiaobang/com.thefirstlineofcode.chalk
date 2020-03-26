package com.firstlinecode.chalk;

import javax.security.cert.X509Certificate;

import com.firstlinecode.chalk.core.stream.IAuthenticationCallback;
import com.firstlinecode.chalk.core.stream.IAuthenticationFailure;
import com.firstlinecode.chalk.core.stream.IAuthenticationToken;
import com.firstlinecode.chalk.core.stream.IStandardStreamer;
import com.firstlinecode.chalk.core.stream.IStreamer;
import com.firstlinecode.chalk.core.stream.NegotiationException;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.core.stream.StandardStreamer;
import com.firstlinecode.chalk.core.stream.StreamConfig;
import com.firstlinecode.chalk.core.stream.UsernamePasswordToken;
import com.firstlinecode.chalk.core.stream.negotiants.sasl.SaslError;
import com.firstlinecode.chalk.core.stream.negotiants.tls.IPeerCertificateTruster;
import com.firstlinecode.chalk.network.ConnectionException;

public class StandardChatClient extends AbstractChatClient implements IAuthenticationCallback {
	protected IPeerCertificateTruster peerCertificateTruster;
	private IAuthenticationFailure authFailure;
	
	public StandardChatClient(StandardStreamConfig streamConfig) {
		super(streamConfig);
	}
	
	public void setPeerCertificateTruster(IPeerCertificateTruster peerCertificateTruster) {
		this.peerCertificateTruster = peerCertificateTruster;
	}

	public IPeerCertificateTruster getPeerCertificateTruster() {
		return peerCertificateTruster;
	}
	
	public void connect(String userName, String password) throws ConnectionException,
			AuthFailureException {
		connect(new UsernamePasswordToken(userName, password));
	}
	
	@Override
	public void connect(IAuthenticationToken authToken) throws ConnectionException, AuthFailureException {
		if (!(authToken instanceof UsernamePasswordToken)) {
			throw new IllegalArgumentException(String.format("Auth token type must be %s.", UsernamePasswordToken.class.getName()));
		}
		
		try {
			super.connect(authToken);
			
			if (authFailure != null) {
				throw new AuthFailureException();
			}
		} catch (RuntimeException e) {
			if ((e.getCause() instanceof NegotiationException) &&
					isMaxFailureCountExcceed((NegotiationException)e.getCause())) {
				throw new AuthFailureException();
			}
			
			throw e;
		}
	}
	
	protected IStreamer createStreamer(StreamConfig streamConfig) {
		IStandardStreamer standardStreamer = new StandardStreamer((StandardStreamConfig)streamConfig);
		standardStreamer.setConnectionListener(this);
		standardStreamer.setNegotiationListener(this);
		standardStreamer.setAuthenticationCallback(this);
		
		if (peerCertificateTruster != null) {
			standardStreamer.setPeerCertificateTruster(peerCertificateTruster);
		} else {
			// always trust peer certificate
			standardStreamer.setPeerCertificateTruster(new IPeerCertificateTruster() {				
				@Override
				public boolean accept(X509Certificate[] certificates) {
					return true;
				}
			});
		}
		
		return standardStreamer;
	}
	
	@Override
	public synchronized void close() {
		if (state == State.CLOSED)
			return;
		
		if (authFailure != null) {
			authFailure.abort();
			authFailure = null;
		}
		
		super.close();
	}
	
	@Override
	public synchronized void failed(IAuthenticationFailure failure) {
		authFailure = failure;
		if (!authFailure.isRetriable())
			state = State.CLOSED;
		
		notify();
	}
	
	@Override
	protected void doConnect(IAuthenticationToken authToken) {
		if (authFailure != null && authFailure.isRetriable()) {
			authFailure.retry(authToken);
			authFailure = null;
		} else {
			super.doConnect(authToken);
		}
	}
	
	protected boolean isMaxFailureCountExcceed(NegotiationException ne) {
		Object additionalErrorInfo =  ne.getAdditionalErrorInfo();
		if ((additionalErrorInfo instanceof SaslError) &&
				(additionalErrorInfo == SaslError.MAX_FAILURE_COUNT_EXCCEED)) {
			return true;
		}
		
		return false;
	}
}
