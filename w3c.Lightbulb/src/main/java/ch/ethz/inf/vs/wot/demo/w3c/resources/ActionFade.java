package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.JsonObject;

import ch.ethz.inf.vs.wot.demo.utils.w3c.ActionResource;
import ch.ethz.inf.vs.wot.demo.w3c.Lightbulb;

import java.awt.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class ActionFade extends ActionResource {
	
	protected AtomicInteger taskNum = new AtomicInteger(0);

	public ActionFade() {
		super("PropertyAction", "Fade", "fade", gson.fromJson("{\"valueType\":{\"duration\":\"xsd:unsignedInteger\",\"target\":\"xsd:string\"}}", JsonObject.class));
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
			
			FadeTask task = new FadeTask(duration, target);
			
			this.add(task);
			
			exchange.setLocationPath(task.getPath()+task.getName());
			exchange.respond(CREATED);
			
		} catch (Exception e) {
			exchange.respond(BAD_REQUEST, "wrong schema");
		}
	}
	
	private class FadeTask extends CoapResource implements Runnable {
		private static final int STEP = 50;
		private int duration;
		private Color target;
		
		public FadeTask(int duration, Color target) {
			super(Integer.toString(taskNum.incrementAndGet()));
			
			this.duration = duration;
			this.target = target;
			
			Lightbulb.tasks.execute(this);
		}

		@Override
		public void run() {
			
			float[] start = Lightbulb.getColor().getRGBColorComponents(null);
			float[] end = target.getRGBColorComponents(null);
			float dr = (end[0] - start[0])*STEP/duration;
			float dg = (end[1] - start[1])*STEP/duration;
			float db = (end[2] - start[2])*STEP/duration;
			
			long tik = System.currentTimeMillis();
			
			// start with 1 for last step outside loop to guard against overshooting
			for (int i=1; i*STEP<duration; ++i) {
				start[0] += dr;
				start[1] += dg;
				start[2] += db;

				try {
					Thread.sleep(STEP);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Lightbulb.setColor(new Color(start[0], start[1], start[2]));
			}
			
			long remaining = duration - (System.currentTimeMillis()-tik);
			
			try {
				Thread.sleep(Math.max(remaining, 0));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Lightbulb.setColor(target);
			
			System.out.println("Fade (ms): " + (System.currentTimeMillis()-tik));
			
			this.delete();
		}
	}
}
