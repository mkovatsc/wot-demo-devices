package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.utils.w3c.ActionResource;
import ch.ethz.inf.vs.wot.demo.w3c.Lightbulb;

import java.awt.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class ActionBlink extends ActionResource {
	
	protected AtomicInteger taskNum = new AtomicInteger(0);

	public ActionBlink() {
		super("PropertyAction", "Blink", "blink");
	}
	
	@Override
	public void handlePOST(CoapExchange exchange) {
		
		BlinkTask task = new BlinkTask();
		
		this.add(task);
		
		exchange.setLocationPath(task.getPath()+task.getName());
		exchange.respond(CREATED, "{\"actions\":[{\"@type\":\"Stop\",\"method\":\"delete\",\"href\":\""+task.getPath()+task.getName()+"\"}]}", APPLICATION_JSON);
	}
	
	private class BlinkTask extends CoapResource implements Runnable {
		private static final int STEP = 50;
		private Future<?> handle;
		
		private float hue = 0;
		private int brightness = 0;
		private int time = 0;
		
		public BlinkTask() {
			super(Integer.toString(taskNum.incrementAndGet()));
			
			handle = Lightbulb.tasks.submit(this);
		}
		
		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(CONTENT, "{\"time\": " + time + ", \"hue\": " + hue + ", \"brightness\": " + brightness + ",\"actions\":[{\"@type\":\"Stop\",\"method\":\"delete\",\"href\":\"\"}]}", APPLICATION_JSON);
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

			PowerRelay.setRelay(true);
			
			try {
			
				// action must be stopped with a delete
				while (true) {
					
					hue = (float) (1f + Math.sin(0.01d * time))/2f;
					brightness = (int) (255*(1f+Math.cos(0.7d * time))/2f);
					
					//System.out.println("{\"time\": " + time + ", \"hue\": " + hue + ", \"brightness\": " + brightness + "}");
					
					Color hsb = Color.getHSBColor(hue, 1f, 1f);
					//System.out.println(hsb.getRed() + "-" + hsb.getGreen() + "-" + hsb.getBlue() + "-" + brightness);
					
					++time;
					
					Thread.sleep(STEP);
					
					Lightbulb.setColor(new Color(hsb.getRed(), hsb.getGreen(), hsb.getBlue(), brightness));
				}
			
			} catch (InterruptedException e) {
				System.out.println("Blink interrupted after " + (System.currentTimeMillis()-tik));
			}
		}
	}
}
