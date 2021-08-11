package com.firstlinecode.chalk.android;

import javax.security.cert.X509Certificate;

import com.firstlinecode.chalk.android.core.stream.StandardStreamer;
import com.firstlinecode.chalk.core.stream.IStandardStreamer;
import com.firstlinecode.chalk.core.stream.IStreamer;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.core.stream.StreamConfig;
import com.firstlinecode.chalk.core.stream.negotiants.tls.IPeerCertificateTruster;
import com.firstlinecode.chalk.network.IConnection;

/**
 * @author xb.zou
 * @date 2017/10/26
 * @option 适用于Android Client的StandardChatClient
 */
public class StandardChatClient extends com.firstlinecode.chalk.core.StandardChatClient {
    public StandardChatClient(StandardStreamConfig streamConfig) {
        super(streamConfig);
    }

    @Override
    protected IStreamer createStreamer(StreamConfig streamConfig, IConnection connection) {
        IStandardStreamer standardStreamer = new StandardStreamer((StandardStreamConfig)streamConfig, connection);
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
}
