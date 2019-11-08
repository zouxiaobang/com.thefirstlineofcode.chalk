package com.firstlinecode.chalk.core.stream.negotiants.tls;

import javax.security.cert.X509Certificate;

public interface IPeerCertificateTruster {
	boolean accept(X509Certificate[] certificates);
}
