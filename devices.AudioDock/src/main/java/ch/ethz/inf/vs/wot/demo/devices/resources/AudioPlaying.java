package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

// t.b.d.
public class AudioPlaying extends CoapResource {
	
	public static MP3Player player = new MP3Player();
	
	public AudioPlaying() {
		super("play");
		getAttributes().setTitle("Now playing");
		getAttributes().addInterfaceDescription("core#p");
		getAttributes().setObservable();

		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, PowerRelay.getRelay() ? ""+player.getPosition() : "", TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

//		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
//			exchange.respond(BAD_REQUEST, "text/plain only");
//			return;
//		}
		
		if (!PowerRelay.getRelay()) {
			exchange.respond(SERVICE_UNAVAILABLE, "turn on first");
			return;
		}
		
		String cmd = exchange.getRequestText();
		
		switch (cmd) {
			case "play": play(); break;
			case "pause": pause(); break;
			case "stop": stop(); break;
			default: goToPos(Integer.parseInt(cmd)); break;
		}

		// complete the request
		exchange.respond(CHANGED);
		
		changed();
	}
	
	private void play() {
		if (player.canResume()) {
			player.resume();
		} else {
			player.stop();
			player.play();
		}
	}
	private void pause() {
		player.pause();
	}
	private void stop() {
		player.stop();
	}
	private void goToPos(int position) {
		
		System.out.println("goto: " + position);
		
		player.stop();
		player.play(position);
	}
}
