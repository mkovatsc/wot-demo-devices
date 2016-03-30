package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class DeviceName extends WoTResource {
	
	private String nameValue = "Osram Superstar";

	public DeviceName() {
		super(Interaction.PROPERTY, "Property", "Name", "n");
		td.addProperty("writeable", true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, nameValue, TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		nameValue = exchange.getRequestText();

		// complete the request
		exchange.respond(CHANGED);
	}
}
