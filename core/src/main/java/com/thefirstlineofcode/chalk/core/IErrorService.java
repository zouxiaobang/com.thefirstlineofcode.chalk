package com.thefirstlineofcode.chalk.core;

import java.util.List;

public interface IErrorService {
	void addErrorListener(IErrorListener listener);
	void removeErrorListener(IErrorListener listener);
	List<IErrorListener> getErrorListeners();
}
