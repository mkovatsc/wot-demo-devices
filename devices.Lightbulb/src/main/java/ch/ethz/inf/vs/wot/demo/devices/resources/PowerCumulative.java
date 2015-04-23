package ch.ethz.inf.vs.wot.demo.devices.resources;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class PowerCumulative extends CoapResource {
	
	private double power = 0d;

	public PowerCumulative() {
		super("kwh");
		getAttributes().setTitle("Cumulative Power");
		getAttributes().addResourceType("ucum:kWh");
		getAttributes().addInterfaceDescription("core#s");
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
