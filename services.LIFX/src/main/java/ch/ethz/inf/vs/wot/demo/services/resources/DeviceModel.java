package ch.ethz.inf.vs.wot.demo.services.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class DeviceModel extends CoapResource {

	public DeviceModel() {
		super("mdl");
		getAttributes().setTitle("Model");
		getAttributes().addInterfaceDescription("core#rp");
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, "LIFX", TEXT_PLAIN);
	}
}