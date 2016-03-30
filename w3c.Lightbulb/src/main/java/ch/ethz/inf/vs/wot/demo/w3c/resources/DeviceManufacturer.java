package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class DeviceManufacturer extends WoTResource {

	public DeviceManufacturer() {
		super(Interaction.PROPERTY, "Property", "Manufacturer", "mfc");
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, "Osram", TEXT_PLAIN);
	}
}
