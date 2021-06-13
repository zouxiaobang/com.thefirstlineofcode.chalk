package com.firstlinecode.chalk.examples;

import java.io.File;
import java.nio.file.Files;

public abstract class AbstractLiteExample extends AbstractExample {
	protected void doInit(Options options) {}
	
	@Override
	public void clean() {
		try {
			File dataDir = new File(options.serverHome, "data");
			if (dataDir.exists()) {
				Files.delete(dataDir.toPath());
			}
		} catch (Exception e) {
			throw new RuntimeException("Can't remove database.", e);
		}
	}

}
