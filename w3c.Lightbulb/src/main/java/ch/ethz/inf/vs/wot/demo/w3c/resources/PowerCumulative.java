package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.server.resources.CoapExchange;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class PowerCumulative extends WoTResource {
	
	private double power = 0d;

	public PowerCumulative() {
		super(Interaction.PROPERTY, "PowerConsumption", "Cumulative Power", "kwh");
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
				power += PowerInstantaneous.getPower()/1000d/60d/60d;

				// Call changed to notify subscribers
				changed();
			}
		}
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, new DecimalFormat("#0.0000").format(power), TEXT_PLAIN);
	}
}
