package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.apache.commons.io.IOUtils;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Timer;
import java.util.TimerTask;

public class DynamicDeviceSemantics extends CoapResource {

	public DynamicDeviceSemantics() {
		super("osemantics");
		getAttributes().addResourceType("semantics");
		getAttributes().setObservable(); // mark observable in the Link-Format
		setObservable(true); // enable observing
		setObserveType(CoAP.Type.CON); // configure the notification type to CONs

		Timer timer = new Timer();
		timer.schedule(new UpdateTask(), 0, 4000);
	}

	private class UpdateTask extends TimerTask {
		@Override
		public void run() {
			// .. periodic update of the resource
			changed(); // notify all observers
		}
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(getClass().getResourceAsStream("thermostat.n3"), writer);
			String theString = writer.toString();
			exchange.respond(theString);
		} catch (IOException e) {
			exchange.reject();
		}
	}
}
