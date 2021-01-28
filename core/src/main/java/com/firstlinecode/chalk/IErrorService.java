package com.firstlinecode.chalk;

import java.util.List;

import com.firstlinecode.chalk.core.IErrorListener;

public interface IErrorService {
	void addErrorListener(IErrorListener listener);
	void removeErrorListener(IErrorListener listener);
	List<IErrorListener> getErrorListeners();
}
