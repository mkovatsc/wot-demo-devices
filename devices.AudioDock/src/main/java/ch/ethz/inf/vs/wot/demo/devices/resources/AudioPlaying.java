package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

// t.b.d.
public class AudioPlaying extends CoapResource {
	
	private static String now = "optical";
	private static CoapResource instance = null;
	
	public static String getNow() {
		return now;
	}
	public static void setNow(String track) {
		now = track;
		if (instance!=null) instance.changed();
	}

	public AudioPlaying() {
		super("now");
		getAttributes().setTitle("Now playing");
		getAttributes().addInterfaceDescription("core#rp");
		getAttributes().setObservable();

		setObservable(true);
		
		instance = this;
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, PowerRelay.getRelay() ? now : "", TEXT_PLAIN);
	}
}
