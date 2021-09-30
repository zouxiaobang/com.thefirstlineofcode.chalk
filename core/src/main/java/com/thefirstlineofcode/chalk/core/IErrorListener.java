package com.thefirstlineofcode.chalk.core;

import com.thefirstlineofcode.basalt.protocol.core.IError;

public interface IErrorListener {
	void occurred(IError error);
}
