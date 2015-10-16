package ch.ethz.inf.vs.wot.demo.services.resources;

import java.awt.Color;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.services.LIFX;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class LEDColor extends CoapResource {
	
	private static Color color = Color.white;

	public LEDColor() {
		super("color");
		getAttributes().setTitle("Color");
		getAttributes().addResourceType("led:color");
		getAttributes().addInterfaceDescription("core#p");
		getAttributes().setObservable();

		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, color.toString(), TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		try {
			color = Color.decode(exchange.getRequestText());
			
			LIFX.bulb.setColor(color);
			
			// complete the request
			exchange.respond(CHANGED);
		
			changed();
			return;
		} catch (NumberFormatException e) {
			exchange.respond(BAD_REQUEST, "#RRGGBB");
		}
	}
}
