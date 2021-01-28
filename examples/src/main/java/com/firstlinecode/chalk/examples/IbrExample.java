package com.firstlinecode.chalk.examples;

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
import com.mongodb.client.model.Filters;

public class IbrExample implements Example, IConnectionListener {
	private IChatClient chatClient;

	@Override
	public void run(Options options) throws RegistrationException {
		chatClient = new StandardChatClient(createStreamConfig(options));
		chatClient.register(IbrPlugin.class);
		
		IRegistration registration = chatClient.createApi(IRegistration.class);
		registration.addConnectionListener(this);
		registration.register(new IRegistrationCallback() {

			@Override
			public Object fillOut(IqRegister iqRegister) {
				if (iqRegister.getRegister() instanceof RegistrationForm) {
					RegistrationForm form = new RegistrationForm();
					form.getFields().add(new RegistrationField("username", "dongger"));
					form.getFields().add(new RegistrationField("password", "a_stupid_man"));
					
					return form;
				} else {
					throw new RuntimeException("Can't get registration form.");
				}
			}
			
		});
		
		chatClient.close();
	}

	private StandardStreamConfig createStreamConfig(Options options) {
		StandardStreamConfig streamConfig = new StandardStreamConfig(options.host, options.port);
		streamConfig.setTlsPreferred(true);
		streamConfig.setResource("chalk_ibr_example");
		
		streamConfig.setProperty(StreamConfig.PROPERTY_NAME_CHALK_MESSAGE_FORMAT, options.messageFormat);
		
		return streamConfig;
	}

	@Override
	public void occurred(ConnectionException exception) {}

	@Override
	public void received(String message) {
		System.out.println("<- " + message);
	}

	@Override
	public void sent(String message) {
		System.out.println("-> " + message);
	}

	@Override
	public void cleanDatabase(MongoDatabase database) {
		database.getCollection("users").deleteOne(Filters.eq("name", "dongger"));
	}

}
