package ch.ethz.inf.vs.wot.demo.services.resources;

import ch.ethz.inf.vs.wot.demo.services.LIFX;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class DeviceSerial extends CoapResource {

	private final LIFX lifx;

	public DeviceSerial(LIFX lifx) {
		super("ser");
		this.lifx = lifx;
		getAttributes().setTitle("Serial");
		getAttributes().addInterfaceDescription("core#rp");
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, lifx.bulb.getLabel(), TEXT_PLAIN);
	}
}
