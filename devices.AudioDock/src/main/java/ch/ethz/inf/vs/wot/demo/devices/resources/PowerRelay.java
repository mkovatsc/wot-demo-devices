package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.devices.AudioDock;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class PowerRelay extends CoapResource {
	
	private static boolean on = false;
	
	public static boolean getRelay() {
		return on;
	}

	public PowerRelay() {
		super("switch");
		getAttributes().setTitle("On/off switch");
		getAttributes().addResourceType("switch");
		getAttributes().addInterfaceDescription("core#a");
		getAttributes().setObservable();

		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, on ? "1":"0", TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		String pl = exchange.getRequestText();
		if (pl.equals("true") || pl.equals("on") || pl.equals("1")) {
			if (on==true) return;
			on = true;
			AudioDock.setSpeakers(true);
		} else if (pl.equals("false") || pl.equals("off") || pl.equals("0")) {
			if (on==false) return;
			on = false;
			AudioDock.setSpeakers(false);
		} else {
			exchange.respond(BAD_REQUEST, "use true/false, on/off, or 1/0");
			return;
		}

		// complete the request
		exchange.respond(CHANGED);
		
		changed();
	}
}
