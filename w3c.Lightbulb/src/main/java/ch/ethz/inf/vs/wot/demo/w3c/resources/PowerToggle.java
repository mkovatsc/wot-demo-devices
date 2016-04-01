package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.utils.w3c.ActionResource;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class PowerToggle extends ActionResource {
	
	protected AtomicInteger taskNum = new AtomicInteger(0);
	
	private ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);

	public PowerToggle() {
		super("Toggle", "Toggle", "toggle");
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		
		exchange.respond(CONTENT, this.getChildren().toString(), TEXT_PLAIN);
	}
	
	@Override
	public void handlePOST(CoapExchange exchange) {
		
		ToggleTask task = new ToggleTask();
		
		this.add(task);
		
		exchange.setLocationPath(task.getPath()+task.getName());
		exchange.respond(CREATED);
	}
	
	private class ToggleTask extends CoapResource implements Runnable {
		
		public ToggleTask() {
			super(Integer.toString(taskNum.incrementAndGet()));
			
			tasks.schedule(this, 0, TimeUnit.MILLISECONDS);
		}

		@Override
		public void run() {
			
			try {
				Thread.sleep(40);
				PowerRelay.setRelay(!PowerRelay.getRelay());
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			this.delete();
		}
	}
}
