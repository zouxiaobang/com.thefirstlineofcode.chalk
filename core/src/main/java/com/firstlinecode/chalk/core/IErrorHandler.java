package com.firstlinecode.chalk.core;

import com.firstlinecode.basalt.protocol.core.IError;

public interface IErrorHandler {
	void process(IError error);
}
