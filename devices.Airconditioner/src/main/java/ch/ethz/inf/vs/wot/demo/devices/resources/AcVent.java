package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.devices.Airconditioner;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class AcVent extends CoapResource {
	
	private static int opened = 0;
	
	public static int getOpened() {
		return opened;
	}

	public AcVent() {
		super("vent");
		getAttributes().setTitle("Air vents");
		getAttributes().addInterfaceDescription("core#a");
		getAttributes().setObservable();

		setObservable(true);
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, ""+opened, TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		if (!PowerRelay.getRelay()) {
			exchange.respond(SERVICE_UNAVAILABLE, "turn on first");
			return;
		}
		
		String pl = exchange.getRequestText();
		if (pl.matches("[0-9]")) {
			int set = Integer.parseInt(pl);
			if ((0 <= set) && (set <= 3)) {
				opened = set;

				// complete the request
				exchange.respond(CHANGED);
				
				// true shows cover, false shows open vent
				Airconditioner.setVent(1, true);
				Airconditioner.setVent(2, true);
				Airconditioner.setVent(3, true);
				
				switch (opened) {
					case 3: Airconditioner.setVent(3, false);
					case 2: Airconditioner.setVent(2, false);
					case 1: Airconditioner.setVent(1, false);
				}
				
				Airconditioner.notifyText("Vent " + opened);
				
				changed();
				return;
			}
		}

		exchange.respond(BAD_REQUEST, "[0-3]");
	}
}
