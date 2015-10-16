package ch.ethz.inf.vs.wot.demo.services.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class DeviceManufacturer extends CoapResource {

	public DeviceManufacturer() {
		super("mfc");
		getAttributes().setTitle("Manufacturer");
		getAttributes().addInterfaceDescription("core#rp");
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, "LiFi Labs Inc.", TEXT_PLAIN);
	}
}
