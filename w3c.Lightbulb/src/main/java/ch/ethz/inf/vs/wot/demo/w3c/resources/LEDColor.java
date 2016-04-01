package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.utils.w3c.PropertyResource;
import ch.ethz.inf.vs.wot.demo.w3c.Lightbulb;

import java.awt.*;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class LEDColor extends PropertyResource {
	
	private static Color color = Color.white;

	public LEDColor() {
		super("RGBColor", "Color", "color", "xsd:string", true);
		getAttributes().setObservable();
		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()), TEXT_PLAIN);
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
