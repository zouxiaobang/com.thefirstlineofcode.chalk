package com.firstlinecode.chalk.core.stream;

import com.firstlinecode.chalk.core.stream.negotiants.tls.IPeerCertificateTruster;

public interface IStandardStreamer extends IStreamer {
	void setPeerCertificateTruster(IPeerCertificateTruster certificateTruster);
	IPeerCertificateTruster getPeerCertificateTruster();
}
