package com.firstlinecode.chalk.examples;

import com.firstlinecode.basalt.xeps.ibr.IqRegister;
import com.firstlinecode.basalt.xeps.ibr.RegistrationField;
import com.firstlinecode.basalt.xeps.ibr.RegistrationForm;
import com.firstlinecode.chalk.core.IChatClient;
import com.firstlinecode.chalk.core.StandardChatClient;
import com.firstlinecode.chalk.xeps.ibr.IRegistration;
import com.firstlinecode.chalk.xeps.ibr.IRegistrationCallback;
import com.firstlinecode.chalk.xeps.ibr.IbrPlugin;

public class IbrLiteExample extends AbstractLiteExample {
	private IChatClient chatClient;
	
	@Override
	public void run() throws Exception {
		chatClient = new StandardChatClient(createStreamConfig(null));
		chatClient.register(IbrPlugin.class);
		
		try {			
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
		} finally {			
			chatClient.close();
		}
	}
	
	@Override
	protected String[][] getUserNameAndPasswords() {
		return null;
	}
}
