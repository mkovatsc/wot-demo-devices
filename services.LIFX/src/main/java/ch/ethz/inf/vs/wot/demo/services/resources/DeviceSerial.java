package ch.ethz.inf.vs.wot.demo.services.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.services.LIFX;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class DeviceSerial extends CoapResource {

	public DeviceSerial() {
		super("ser");
		getAttributes().setTitle("Serial");
		getAttributes().addInterfaceDescription("core#rp");
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, LIFX.bulb.getLabel(), TEXT_PLAIN);
	}
}
