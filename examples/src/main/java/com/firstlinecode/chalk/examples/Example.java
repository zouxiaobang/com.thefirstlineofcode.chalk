package com.firstlinecode.chalk.examples;

public interface Example {
	void init(Options options);
	void run() throws Exception;
	void clean();
}
