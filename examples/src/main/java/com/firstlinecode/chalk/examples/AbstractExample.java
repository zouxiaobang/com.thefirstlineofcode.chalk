package com.firstlinecode.chalk.examples;

import org.bson.Document;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.chalk.core.IChatClient;
import com.firstlinecode.chalk.core.StandardChatClient;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.core.stream.StreamConfig;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.IConnectionListener;
import com.firstlinecode.chalk.xeps.ibr.IRegistration;
import com.firstlinecode.chalk.xeps.ibr.IRegistrationCallback;
import com.firstlinecode.chalk.xeps.ibr.IbrPlugin;
import com.firstlinecode.chalk.xeps.ibr.RegistrationException;
import com.firstlinecode.basalt.xeps.ibr.IqRegister;
import com.firstlinecode.basalt.xeps.ibr.RegistrationField;
import com.firstlinecode.basalt.xeps.ibr.RegistrationForm;
import com.mongodb.client.MongoDatabase;

public abstract class AbstractExample implements Example, IConnectionListener {
	protected Options options;

	@Override
	public void run(Options options) throws Exception {
		this.options = options;
		createUsers();
		runExample();
	}
	
	@Override
	public void cleanDatabase(MongoDatabase database) {
		cleanExampleData(database);
		cleanUsers(database);
	}

	protected void cleanUsers(MongoDatabase database) {
		database.getCollection("users").deleteMany(new Document());
	}
	
	protected abstract void runExample() throws Exception;
	protected abstract String[][] getUserNameAndPasswords();	
	protected abstract void cleanExampleData(MongoDatabase database);
	
	protected StandardStreamConfig createStreamConfig(String resource) {
		StandardStreamConfig streamConfig = new StandardStreamConfig(options.host, options.port);
		streamConfig.setTlsPreferred(true);
		streamConfig.setResource(resource);
		
		streamConfig.setProperty(StreamConfig.PROPERTY_NAME_CHALK_MESSAGE_FORMAT, options.messageFormat);
		
		return streamConfig;
	}
	
	protected JabberId getJabberId(String user) {
		return getJabberId(user, null);
	}
	
	protected JabberId getJabberId(String user, String resource) {
		if (resource == null) {
			return JabberId.parse(String.format("%s@%s", user, options.host));			
		}
		
		return JabberId.parse(String.format("%s@%s/%s", user, options.host, resource));
	}
	
	protected StandardStreamConfig createStreamConfig() {
		return createStreamConfig("chalk_" + getExampleName() + "_example");
	}

	protected String getExampleName() {
		String className = getClass().getSimpleName();
		if (className.endsWith("Example")) {
			return className.substring(0, className.length() - 7);
		}
		
		throw new IllegalArgumentException("Can't determine example name. You should override getExampleName() method to resolve the problem.");
	}

	protected void createUsers() throws RegistrationException {
		IChatClient chatClient = new StandardChatClient(createStreamConfig());
		chatClient.register(IbrPlugin.class);
		
		IRegistration registration = chatClient.createApi(IRegistration.class);
		registration.addConnectionListener(this);
		
		for (final String[] userNameAndPassword : getUserNameAndPasswords()) {
			registration.register(new IRegistrationCallback() {

				@Override
				public Object fillOut(IqRegister iqRegister) {
					if (iqRegister.getRegister() instanceof RegistrationForm) {
						RegistrationForm form = new RegistrationForm();
						form.getFields().add(new RegistrationField("username", userNameAndPassword[0]));
						form.getFields().add(new RegistrationField("password", userNameAndPassword[1]));
						
						return form;
					} else {
						throw new RuntimeException("Can't get registration form.");
					}
				}
				
			});
		}
		
		chatClient.close();
	}
	
	@Override
	public void occurred(ConnectionException exception) {}

	@Override
	public void received(String message) {
		printString("<- " + message);
	}

	@Override
	public void sent(String message) {
		printString("-> " + message);
	}
	
	protected void printString(String string) {
		System.out.println(string);
	}
	
	protected void printException(Exception e) {
		System.out.println("Exception:");
		e.printStackTrace(System.out);
		System.out.println();
	}

}
