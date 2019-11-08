package com.firstlinecode.chalk.demo;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.firstlinecode.chalk.IChatClient;
import com.firstlinecode.chalk.StandardChatClient;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.core.stream.StreamConfig;
import com.firstlinecode.chalk.demo.Demo.Protocol;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.IConnectionListener;
import com.firstlinecode.chalk.xeps.ibr.IRegistration;
import com.firstlinecode.chalk.xeps.ibr.IRegistrationCallback;
import com.firstlinecode.chalk.xeps.ibr.IbrPlugin;
import com.firstlinecode.chalk.xeps.ibr.RegistrationException;
import com.firstlinecode.basalt.xeps.ibr.IqRegister;
import com.firstlinecode.basalt.xeps.ibr.RegistrationField;
import com.firstlinecode.basalt.xeps.ibr.RegistrationForm;

public class Main {
	public static void main(String[] args) throws RegistrationException {
		new Main().run(args);
	}
	
	private class Config {
		public Protocol protocol = Protocol.LEP;
		public String host = "localhost";
		public int port = 5222;
		public String messageFormat = "xml";
	}
	
	private void run(String[] args) throws RegistrationException {
		Config config;
		try {
			config = getConfig(args);
		} catch (IllegalArgumentException e) {
			printUsage();
			return;
		}
		
		createAccounts(config);
		
		System.setProperty("chalk.stream.config.host", config.host);
		System.setProperty("chalk.stream.config.port", Integer.toString(config.port));
		System.setProperty("chalk.stream.config.message.format", config.messageFormat.toString().toLowerCase());
		
		new com.firstlinecode.chalk.demo.Demo().run(config.protocol);
	}

	private void createAccounts(Config config) throws RegistrationException {
		StandardStreamConfig streamConfig = new StandardStreamConfig(config.host, config.port);
		streamConfig.setTlsPreferred(false);
		if (config.messageFormat != null)
			streamConfig.setProperty(StreamConfig.PROPERTY_NAME_CHALK_MESSAGE_FORMAT, config.messageFormat);
		IChatClient chatClient = new StandardChatClient(streamConfig);
		chatClient.register(IbrPlugin.class);
		
		String[][] accounts = new String[][] {
			{"dongger", "a_stupid_man"},
			{"agilest", "a_good_guy"},
			{"smartsheep", "a_pretty_girl"},
			{"jelly", "another_pretty_girl"}
		};
		
		for (String[] account : accounts) {
			createAccount(chatClient, account);
		}
	}

	private void createAccount(IChatClient chatClient, final String[] account) throws RegistrationException {
		IRegistration registration = chatClient.createApi(IRegistration.class);
		registration.addConnectionListener(new IConnectionListener() {
			
			@Override
			public void sent(String message) {
				printOut(message);
			}
			
			@Override
			public void received(String message) {
				printIn(message);
			}
			
			@Override
			public void occurred(ConnectionException exception) {
				OutputStream os = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(os);
				exception.printStackTrace(ps);
				print(String.format("Connection error: %s, %s.", exception.getType(), os.toString()));
			}
		});
		
		registration.register(new RegistrationCallback(account[0], account[1]));
	}
	
	private void print(String message) {
		System.out.println(String.format("\t%s: %s.", "Main", message));
	}
	
	private void printOut(String message) {
		System.out.println(String.format("->%s: %s.", "Main", message));
	}
	
	private void printIn(String message) {
		System.out.println(String.format("<-%s: %s.", "Main", message));
	}
	
	private class RegistrationCallback implements IRegistrationCallback {
		private String name;
		private String password;
		
		public RegistrationCallback(String name, String password) {
			this.name = name;
			this.password = password;
		}

		@Override
		public Object fillOut(IqRegister iqRegister) {
			if (iqRegister.getRegister() instanceof RegistrationForm) {
				RegistrationForm form = new RegistrationForm();
				form.getFields().add(new RegistrationField("username", name));
				form.getFields().add(new RegistrationField("password", password));
				
				return form;
			} else {
				throw new RuntimeException("Can't get registration form.");
			}
		}
		
	}

	private Config getConfig(String[] args) throws IllegalArgumentException {
		Config config = new Config();
		
		Map<String, String> mArgs = new HashMap<>();
		for (String arg : args) {
			if (!arg.startsWith("--")) {
				throw new IllegalArgumentException();
			}
			
			int equalMarkIndex = arg.indexOf('=');
			if (equalMarkIndex == -1) {
				throw new IllegalArgumentException();
			}
			
			String argName = arg.substring(2, equalMarkIndex);
			String argValue = arg.substring(equalMarkIndex + 1, arg.length());
			
			if (argName == null || argValue == null) {
				throw new IllegalArgumentException();
			}
			
			if (mArgs.containsKey(argName)) {
				throw new IllegalArgumentException();
			}
			
			mArgs.put(argName, argValue);
		}
		
		for (Map.Entry<String, String> entry : mArgs.entrySet()) {
			if ("protocol".equals(entry.getKey())) {
				String value = entry.getValue();
				if ("lep".equals(value)) {
					config.protocol = Protocol.LEP;
				} else if ("standard".equals(value)) {
					config.protocol = Protocol.STANDARD;
				} else {
					throw new IllegalArgumentException();
				}
				
			} else if ("host".equals(entry.getKey())) {
				config.host = entry.getValue();
			} else if ("port".equals(entry.getKey())) {
				config.port = Integer.parseInt(entry.getValue());
			} else if ("message-format".equals(entry.getKey())) {
				String value = entry.getValue();
				if ("xml".equals(value) || "binary".equals(value)) {
					config.messageFormat = value;
				} else {
					throw new IllegalArgumentException(String.format("Invalid message format: %s.", value));
				}
			} else {
				throw new IllegalArgumentException();
			}
		}
		
		return config;
	}

	private void printUsage() {
		System.out.println("java com.firstlinecode.chalk.demo.Main [OPTIONS]");
		System.out.println("OPTIONS:");
		System.out.println("--protocol=[]\tProtocol(lep or standard)");
		System.out.println("--host=[]\tServer address");
		System.out.println("--port=[]\tServer port");
		System.out.println("--message-format=[]\tMessage format(xml or binary)");
	}
}
