package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class TvInput extends CoapResource {
	
	private static String in = "TV";
	
	public static String getInput() {
		return in;
	}

	public TvInput() {
		super("in");
		getAttributes().setTitle("Input");
		getAttributes().addInterfaceDescription("core#p");
		getAttributes().setObservable();

		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, in, TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {
		
		if (!PowerRelay.getRelay()) {
			exchange.respond(SERVICE_UNAVAILABLE);
			return;
		}

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		String pl = exchange.getRequestText();
		
		if (!pl.equals("TV") && !pl.equals("SAT") &&
				!pl.equals("HDMI1") && !pl.equals("HDMI2") && !pl.equals("HDMI3") &&
				!pl.equals("VGA") && !pl.equals("EXT") && !pl.equals("YPbPr") &&
				!pl.startsWith("http")) {
			exchange.respond(BAD_REQUEST, "TV, SAT, HDMI1--3, VGA, EXT, YPbPr, HTTP streams");
			return;
		}
		
		in = pl;
		
		// complete the request
		exchange.respond(CHANGED);
		
		changed();
	}
}
