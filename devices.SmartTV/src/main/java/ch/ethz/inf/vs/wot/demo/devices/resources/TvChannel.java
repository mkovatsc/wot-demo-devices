package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.devices.SmartTV;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class TvChannel extends CoapResource {
	
	private static int channel = 1;
	
	public static int getChannel() {
		return channel;
	}

	public TvChannel() {
		super("ch");
		getAttributes().setTitle("Channel");
		getAttributes().addInterfaceDescription("core#a");
		getAttributes().setObservable();

		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, ""+channel, TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {
		
		if (!PowerRelay.getRelay()) {
			exchange.respond(SERVICE_UNAVAILABLE);
			return;
		}

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		String pl = exchange.getRequestText();
		if (pl.matches("[0-9]+")) {
			int ch = Integer.parseInt(pl);
			if ((0 <= ch) && (ch <= 312)) {
				channel = ch;
				SmartTV.notifyText("CHANNEL " + channel);

				// complete the request
				exchange.respond(CHANGED);
				
				changed();
				return;
			}
		}
		
		exchange.respond(BAD_REQUEST, "0--312");
	}
}
