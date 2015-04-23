package ch.ethz.inf.vs.wot.demo.devices.resources;

import java.text.DecimalFormat;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.devices.Airconditioner;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class AcTemperature extends CoapResource {
	
	private static float target = 23f;
	
	public static float getTarget() {
		return target;
	}

	public AcTemperature() {
		super("target");
		getAttributes().setTitle("Target temperature");
		getAttributes().addResourceType("ucum:Cel"); // see unitsofmeasure.org
		getAttributes().addInterfaceDescription("core#a");
		getAttributes().setObservable();

		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, new DecimalFormat("#0.0").format(target), TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		String pl = exchange.getRequestText();
		if (!pl.matches("[0-9]+(\\.[0-9]+)?")) {
			exchange.respond(BAD_REQUEST, "15.0°C -- 28.0°C");
			return;
		}
		
		float set = Float.parseFloat(pl);
		if (set < 15f) set = 15f;
		if (set > 28f) set = 28f;
		
		target = set;
		exchange.respond(CHANGED);
		
		Airconditioner.notifyText(new DecimalFormat("To 0.0°C").format(target));
		
		changed();
	}
}
