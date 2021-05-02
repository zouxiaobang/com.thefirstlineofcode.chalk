package com.firstlinecode.chalk.xeps.ibr;

import java.util.ArrayList;
import java.util.List;

import javax.security.cert.X509Certificate;

import com.firstlinecode.basalt.oxm.convention.NamingConventionParserFactory;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stream.Features;
import com.firstlinecode.basalt.xeps.ibr.Register;
import com.firstlinecode.chalk.core.AbstractChatClient;
import com.firstlinecode.chalk.core.stream.AbstractStreamer;
import com.firstlinecode.chalk.core.stream.IStreamNegotiant;
import com.firstlinecode.chalk.core.stream.IStreamer;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.core.stream.StreamConfig;
import com.firstlinecode.chalk.core.stream.negotiants.InitialStreamNegotiant;
import com.firstlinecode.chalk.core.stream.negotiants.tls.IPeerCertificateTruster;
import com.firstlinecode.chalk.core.stream.negotiants.tls.TlsNegotiant;
import com.firstlinecode.chalk.network.IConnection;
import com.firstlinecode.chalk.network.SocketConnection;

class IbrChatClient extends AbstractChatClient {
	private IPeerCertificateTruster peerCertificateTruster;

	public IbrChatClient(StreamConfig streamConfig) {
		super(streamConfig, new SocketConnection());
	}
	
	public IbrChatClient(StreamConfig streamConfig, IConnection connection) {
		super(streamConfig, connection);
	}
	
	public void setPeerCertificateTruster(IPeerCertificateTruster peerCertificateTruster) {
		this.peerCertificateTruster = peerCertificateTruster;
	}

	public IPeerCertificateTruster getPeerCertificateTruster() {
		return peerCertificateTruster;
	}

	@Override
	protected IStreamer createStreamer(StreamConfig streamConfig, IConnection connection) {
		IbrStreamer streamer = new IbrStreamer(getStreamConfig(), connection);
		streamer.setConnectionListener(this);
		streamer.setNegotiationListener(this);
		
		if (peerCertificateTruster != null) {
			streamer.setPeerCertificateTruster(peerCertificateTruster);
		} else {
			// always trust all peer certificates.
			streamer.setPeerCertificateTruster(new IPeerCertificateTruster() {				
				@Override
				public boolean accept(X509Certificate[] certificates) {
					return true;
				}
			});
		}
		
		return streamer;
	}

	private class IbrStreamer extends AbstractStreamer {
		private IPeerCertificateTruster certificateTruster;
		
		public IbrStreamer(StreamConfig streamConfig, IConnection connection) {
			super(streamConfig, connection);
		}
		
		@Override
		protected List<IStreamNegotiant> createNegotiants() {
			List<IStreamNegotiant> negotiants = new ArrayList<>();
			
			InitialStreamNegotiant initialStreamNegotiant = createIbrSupportedInitialStreamNegotiant();
			negotiants.add(initialStreamNegotiant);
			
			TlsNegotiant tls = createIbrSupportedTlsNegotiant();
			negotiants.add(tls);
			
			IbrNegotiant ibr = createIbrNegotiant();
			negotiants.add(ibr);
			
			setNegotiationReadResponseTimeout(negotiants);
			
			return negotiants;
		}

		private IbrNegotiant createIbrNegotiant() {
			return new IbrNegotiant();
		}
		
		public void setPeerCertificateTruster(IPeerCertificateTruster certificateTruster) {
			this.certificateTruster = certificateTruster;
		}

		private InitialStreamNegotiant createIbrSupportedInitialStreamNegotiant() {
			return new IbrSupportedInitialStreamNegotiant(streamConfig.getHost(), streamConfig.getLang());
		}
		
		private TlsNegotiant createIbrSupportedTlsNegotiant() {
			TlsNegotiant tls = new IbrSupportedTlsNegotiant(streamConfig.getHost(), streamConfig.getLang(),
					((StandardStreamConfig)streamConfig).isTlsPreferred());
			tls.setPeerCertificateTruster(certificateTruster);
			return tls;
		}
	}
	
	private static class IbrSupportedInitialStreamNegotiant extends InitialStreamNegotiant {
		
		static {
			oxmFactory.register(ProtocolChain.first(Features.PROTOCOL).next(Register.PROTOCOL),
					new NamingConventionParserFactory<>(Register.class));
		}

		public IbrSupportedInitialStreamNegotiant(String hostName, String lang) {
			super(hostName, lang);
		}
		
	}
	
	private static class IbrSupportedTlsNegotiant extends TlsNegotiant {
		
		static {
			oxmFactory.register(ProtocolChain.first(Features.PROTOCOL).next(Register.PROTOCOL),
					new NamingConventionParserFactory<>(Register.class));
		}

		public IbrSupportedTlsNegotiant(String hostName, String lang, boolean tlsPreferred) {
			super(hostName, lang, tlsPreferred);
		}
		
	}

	@Override
	protected IConnection createConnection() {
		return new SocketConnection();
	}
	
}
