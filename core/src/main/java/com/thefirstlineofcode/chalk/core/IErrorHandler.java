package com.thefirstlineofcode.chalk.core;

import com.thefirstlineofcode.basalt.protocol.core.IError;

public interface IErrorHandler {
	void process(IError error);
}
