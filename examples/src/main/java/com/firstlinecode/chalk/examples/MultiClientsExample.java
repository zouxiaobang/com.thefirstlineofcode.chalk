package com.firstlinecode.chalk.examples;

import com.firstlinecode.chalk.core.StandardChatClient;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;

public abstract class MultiClientsExample extends AbstractExample {
	private int activatedClients;
	
	@Override
	protected void runExample() throws Exception {
		Thread[] clients = createClients();
		activatedClients = clients.length;
		
		for (Thread client : clients) {
			client.start();
		}
		
		synchronized (this) {
			wait();
		}
	}
	
	protected class ChatClient extends StandardChatClient {
		public ChatClient(StandardStreamConfig streamConfig) {
			super(streamConfig);
		}
		
		@Override
		public synchronized void close() {
			super.close();
			
			synchronized (MultiClientsExample.this) {
				activatedClients--;
				if (activatedClients == 0) {
					MultiClientsExample.this.notify();
				}
			}
		}
		
		@Override
		protected synchronized void close(boolean graceful) {
			super.close(graceful);
			
			synchronized (MultiClientsExample.this) {
				activatedClients--;
				if (activatedClients == 0) {
					MultiClientsExample.this.notify();
				}
			}
		}
	}
	
	protected abstract AbstractClientThread[] createClients();

}
