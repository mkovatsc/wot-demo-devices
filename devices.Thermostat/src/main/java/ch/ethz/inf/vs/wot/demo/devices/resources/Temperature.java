package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * @author Yassin N. Hassan
 * @since 02.11.14.
 */
public class Temperature extends CoapResource {

	public Temperature() {
		super("temperature");
		getAttributes().addResourceType("temperature");
		getAttributes().addResourceType("ucum:degF"); // see unitsofmeasure.org
	}

	@Override
	public void handlePUT(CoapExchange exchange) {
		exchange.respond(CoAP.ResponseCode.CHANGED);
	}
}
