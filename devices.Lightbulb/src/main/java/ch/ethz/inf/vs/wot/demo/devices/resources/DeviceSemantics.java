package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.apache.commons.io.IOUtils;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.io.IOException;
import java.io.StringWriter;

public class DeviceSemantics extends CoapResource {

    public DeviceSemantics() {
        super("semantics");
        getAttributes().addResourceType("semantics");
    }

	@Override
	public void handleGET(CoapExchange exchange) {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(getClass().getResourceAsStream("lightbulb.n3"), writer);
			String theString = writer.toString();
			exchange.respond(theString);
		} catch (IOException e) {
			exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
		}
	}
}
