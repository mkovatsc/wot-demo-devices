package ch.ethz.inf.vs.wot.demo.w3c.resources;

import ch.ethz.inf.vs.wot.demo.w3c.Lightbulb;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

import org.eclipse.californium.core.server.resources.CoapExchange;

public class PowerRelay extends WoTResource {
	
	private static boolean on = false;
	
	public static boolean getRelay() {
		return on;
	}

	public PowerRelay() {
		super(Interaction.ACTION, "Switch", "On/off switch", "switch");
		getAttributes().setObservable();
		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, on ? "1":"0", TEXT_PLAIN);
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
