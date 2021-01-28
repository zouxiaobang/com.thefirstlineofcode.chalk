package com.firstlinecode.chalk.examples;

import com.mongodb.client.MongoDatabase;

public interface Example {
	void run(Options options) throws Exception;
	void cleanDatabase(MongoDatabase database);
}
