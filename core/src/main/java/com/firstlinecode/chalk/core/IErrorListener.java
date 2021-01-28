package com.firstlinecode.chalk.core;

import com.firstlinecode.basalt.protocol.core.IError;

public interface IErrorListener {
	void occurred(IError error);
}
