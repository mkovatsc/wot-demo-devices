package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Arrays;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class AudioInput extends CoapResource {
	
	private static String[] inputs = {"internal", "USB", "aux", "optical"};
	private static String in = inputs[0];
	
	public static String getInput() {
		return in;
	}

	public AudioInput() {
		super("in");
		getAttributes().setTitle("Input");
		getAttributes().addInterfaceDescription("core#p");
		getAttributes().setObservable();

		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, in, TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		in = exchange.getRequestText();
		
		if (!Arrays.asList(inputs).contains(in)) {
			exchange.respond(BAD_REQUEST, Arrays.toString(inputs));
			return;
		}

		// complete the request
		exchange.respond(CHANGED);
		
		changed();
	}
}
