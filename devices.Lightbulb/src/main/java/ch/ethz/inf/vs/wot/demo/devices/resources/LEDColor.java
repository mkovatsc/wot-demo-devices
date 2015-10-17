package ch.ethz.inf.vs.wot.demo.devices.resources;

import ch.ethz.inf.vs.wot.demo.devices.Lightbulb;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.awt.*;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

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
			
			// complete the request
			exchange.respond(CHANGED);
			
			Lightbulb.setColor(color);
		
			changed();
			return;
		} catch (NumberFormatException e) {
			exchange.respond(BAD_REQUEST, "#RRGGBB");
		}
	}
}
