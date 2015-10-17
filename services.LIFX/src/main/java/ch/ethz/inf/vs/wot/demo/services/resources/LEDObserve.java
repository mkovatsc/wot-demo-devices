package ch.ethz.inf.vs.wot.demo.services.resources;

import ch.ethz.inf.vs.wot.demo.services.LIFX;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.awt.*;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class LEDObserve extends CoapResource {
	
	private static String uri = "";
	private static CoapClient client = null;
	private static CoapObserveRelation handle = null;
	private final LIFX lifx;

	public LEDObserve(LIFX lifx) {
		super("obs");
		this.lifx = lifx;
		getAttributes().setTitle("Follow color");
		getAttributes().addResourceType("led:obs");
		getAttributes().addInterfaceDescription("core#p");
		getAttributes().setObservable();

		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, uri, TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		String in = exchange.getRequestText();
		
		if (in.startsWith("coap://")) {

			exchange.respond(CHANGED);
			
			if (handle!=null) handle.proactiveCancel();
			
			client = this.createClient(in);
			handle = client.observeAndWait(new CoapHandler() {
				
				@Override
				public void onLoad(CoapResponse response) {
					try {
						lifx.bulb.setColor( Color.decode(response.getResponseText()) );
					} catch (NumberFormatException e) {
						handle.proactiveCancel();
					}
				}
				
				@Override
				public void onError() {
					handle.reactiveCancel();
				}
			});
			
		
			changed();
			return;
		} else {
			exchange.respond(BAD_REQUEST, "coap URI");
		}
	}
}
