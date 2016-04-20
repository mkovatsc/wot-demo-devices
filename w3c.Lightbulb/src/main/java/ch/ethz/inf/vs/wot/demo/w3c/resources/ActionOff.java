package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.JsonObject;

import ch.ethz.inf.vs.wot.demo.utils.w3c.ActionResource;
import ch.ethz.inf.vs.wot.demo.w3c.Lightbulb;

import java.awt.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_JSON;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class ActionOff extends ActionResource {
	
	protected AtomicInteger taskNum = new AtomicInteger(0);

	public ActionOff() {
		super("Off", "Fade out", "off", gson.fromJson("{\"valueType\":[null,\"xsd:unsignedInteger\"]}", JsonObject.class));
	}

	
	@Override
	public void handlePOST(CoapExchange exchange) {
		
		int duration = 0;
		
		try {
			duration = Integer.parseInt(exchange.getRequestText());
		} catch (NumberFormatException e) {
			// 0
		}
		
		OffTask task = new OffTask(duration);
		
		this.add(task);
		
		exchange.setLocationPath(task.getPath()+task.getName());
		exchange.respond(CREATED);
	}
	
	private class OffTask extends CoapResource implements Runnable {
		private static final int STEP = 50;
		private int duration;
		private Future<?> handle;
		
		public OffTask(int duration) {
			super(Integer.toString(taskNum.incrementAndGet()));
			
			this.duration = duration;
			
			handle = Lightbulb.tasks.submit(this);
		}
		
		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(CONTENT, ""+duration, TEXT_PLAIN);
		};
		
		@Override
		public void handleDELETE(CoapExchange exchange) {
			handle.cancel(true);
			this.delete();
			exchange.respond(DELETED);
		};

		@Override
		public void run() {
			
			long tik = System.currentTimeMillis();
			
			try {
			
				if (PowerRelay.getRelay()) {
				
					float[] start = Lightbulb.getColor().getRGBComponents(null);
					float d = (start[3])*STEP/duration;
	
					// start with 1 for last step outside loop to guard against overshooting
					for (int i=1; i*STEP<duration; ++i) {
						start[3] -= d;
						Thread.sleep(STEP);
						Lightbulb.setColor(new Color(start[0], start[1], start[2], start[3]));
					}
					
					long remaining = duration - (System.currentTimeMillis()-tik);
					
					Thread.sleep(Math.max(remaining, 0));
					
					PowerRelay.setRelay(false);
					
					System.out.println("Fade out (ms): " + (System.currentTimeMillis()-tik));
					
				} else {
					System.out.println("Already off");
				}
			
				this.delete();
			} catch (InterruptedException e) {
				System.out.println("Fade out interrupted after " + (System.currentTimeMillis()-tik));
			}
		}
	}
}
