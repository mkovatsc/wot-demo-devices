package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.w3c.utils.PropertyResource;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class DeviceSerial extends PropertyResource {

	public DeviceSerial() {
		super("Property", "Serial", "ser", "xsd:string", false);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, ""+Math.pow(EndpointManager.getEndpointManager().getDefaultEndpoint().getAddress().getPort(), 2), TEXT_PLAIN);
	}
}
