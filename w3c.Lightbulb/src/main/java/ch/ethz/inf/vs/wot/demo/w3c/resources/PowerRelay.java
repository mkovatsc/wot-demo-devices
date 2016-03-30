package ch.ethz.inf.vs.wot.demo.w3c.resources;

import ch.ethz.inf.vs.wot.demo.w3c.Lightbulb;
import ch.ethz.inf.vs.wot.demo.w3c.utils.PropertyResource;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.JsonArray;

public class PowerRelay extends PropertyResource {
	
	private static boolean on = false;
	
	public static boolean getRelay() {
		return on;
	}

	public static void setRelay(boolean on) {
		PowerRelay.on = on;
		Lightbulb.update();
	}

	public PowerRelay() {
		super("OnOff", "On/off switch", "switch", "xsd:string", true);
		getAttributes().setObservable();
		setObservable(true);
		
		this.td.add("enum", gson.fromJson("[\"on\",\"off\"]", JsonArray.class));
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, on ? "on":"off", TEXT_PLAIN);
	}
	
	@Override
	public void handlePOST(CoapExchange exchange) {

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		String pl = exchange.getRequestText();
		if (pl.equals("true") || pl.equals("on") || pl.equals("1")) {
			on = true;
			Lightbulb.update();
		} else if (pl.equals("false") || pl.equals("off") || pl.equals("0")) {
			on = false;
			Lightbulb.update();
		} else {
			exchange.respond(BAD_REQUEST, "use true/false, on/off, or 1/0");
			return;
		}

		// complete the request
		exchange.respond(CHANGED);
		
		changed();
	}
}
