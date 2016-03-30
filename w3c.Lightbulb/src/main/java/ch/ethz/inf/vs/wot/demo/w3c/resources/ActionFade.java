package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.JsonObject;

import ch.ethz.inf.vs.wot.demo.w3c.Lightbulb;

import java.awt.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_JSON;

public class ActionFade extends WoTResource {
	
	private static Color color = Color.white;
	protected AtomicInteger taskNum = new AtomicInteger(0);
	
	private ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);

	public ActionFade() {
		super(Interaction.ACTION, "PropertyAction", "Fade", "fade");
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		
		exchange.respond(CONTENT, this.getChildren().toString(), TEXT_PLAIN);
	}
	
	@Override
	public void handlePOST(CoapExchange exchange) {
		
		try {
			JsonObject json = gson.fromJson(exchange.getRequestText(), JsonObject.class);
			
			Color target = Color.decode(json.get("target").getAsString());
			int duration = json.get("duration").getAsInt();
			
			this.add(new FadeTask(duration, target));
			
		} catch (Exception e) {
			exchange.respond(BAD_REQUEST, "wrong schema");
		}
	}
	
	private class FadeTask extends CoapResource implements Runnable {
		
		private int duration;
		private Color target;
		
		public FadeTask(int duration, Color target) {
			super(Integer.toString(taskNum.incrementAndGet()));
			
			this.duration = duration;
			this.target = target;
			
			tasks.schedule(this, 0, TimeUnit.MILLISECONDS);
		}

		@Override
		public void run() {
			
			while (!color.equals(target)) {
				
				Lightbulb.setColor(target);
				color = target;
				
				try {
					Thread.sleep(duration);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			this.delete();
		}
	}
}
