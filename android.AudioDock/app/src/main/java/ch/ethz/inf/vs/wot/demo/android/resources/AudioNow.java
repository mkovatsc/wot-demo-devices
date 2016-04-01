package ch.ethz.inf.vs.wot.demo.android.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.inf.vs.wot.demo.android.R;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class AudioNow extends CoapResource {
	
	private static final Map<String, Integer> songs;
	static
	{
		songs = new HashMap<>();
		songs.put("bohemian_rhapsody.mp3", R.raw.bohemian_rhapsody);
		songs.put("gangnam_style.mp3", R.raw.gangnam_style);
		songs.put("fur_elise.mp3", R.raw.fur_elise);
	}
	private static String song = "";
	
	public static String getSong() {
		return song;
	}

	public AudioNow() {
		super("now");
		getAttributes().setTitle("Now playing");
		getAttributes().addInterfaceDescription("core#p");
		getAttributes().setObservable();

		setObservable(true);

	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, song, TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

//		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
//			exchange.respond(BAD_REQUEST, "text/plain only");
//			return;
//		}
		
		if (!songs.containsKey(exchange.getRequestText())) {
			exchange.respond(BAD_REQUEST, Arrays.toString(songs.keySet().toArray()));
		} else {
			song = exchange.getRequestText();
			exchange.respond(CHANGED);

			try {
				AudioPlaying.player.setSong(song, songs.get(song));
			} catch (IOException e) {
				e.printStackTrace();
			}
			//AudioPlaying.player.play();
			changed();
		}
	}
}
