package ch.ethz.inf.vs.wot.demo.services.resources;

import ch.ethz.inf.vs.wot.demo.services.LIFX;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.awt.*;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class LEDColor extends CoapResource {
	
	private static Color color = Color.white;
	private final LIFX lifx;

	public LEDColor(LIFX lifx) {
		super("color");
		this.lifx = lifx;
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

			lifx.bulb.setColor(color);
			lifx.bulb.setColor(color);
			
			// complete the request
			exchange.respond(CHANGED);
		
			changed();
			return;
		} catch (NumberFormatException e) {
			exchange.respond(BAD_REQUEST, "#RRGGBB");
		}
	}
}
