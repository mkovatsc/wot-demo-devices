package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.server.resources.CoapExchange;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class PowerInstantaneous extends WoTResource {
	
	private static double power = 0d;
	
	public static double getPower() {
		return power;
	}

	public PowerInstantaneous() {
		super(Interaction.PROPERTY, "Property", "Instantaneous Power", "w");
		getAttributes().setObservable();
		setObservable(true);

		// Set timer task scheduling
		Timer timer = new Timer();
		timer.schedule(new TimeTask(), 0, 1000);
	}

	private class TimeTask extends TimerTask {

		@Override
		public void run() {
			if (PowerRelay.getRelay()) {
				power = Math.round( 200d + 5d*Math.random() ) / 1000f;
			} else {
				// skip changed() update if nothing changed
				if (power==0d) {
					return;
				}
				
				power = 0d;
			}

			// Call changed to notify subscribers
			changed();
		}
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, new DecimalFormat("##.00").format(power), TEXT_PLAIN);
	}
}
