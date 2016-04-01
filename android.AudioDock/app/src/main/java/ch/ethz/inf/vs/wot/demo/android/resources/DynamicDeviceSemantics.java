package ch.ethz.inf.vs.wot.demo.android.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import ch.ethz.inf.vs.wot.demo.android.AudioDock;
import ch.ethz.inf.vs.wot.demo.android.R;

public class DynamicDeviceSemantics extends CoapResource {

	private static DynamicDeviceSemantics instance;

	public DynamicDeviceSemantics() {
		super("statedesc");
		getAttributes().addResourceType("semantics");
		getAttributes().setObservable(); // mark observable in the Link-Format
		setObservable(true); // enable observing
		setObserveType(CoAP.Type.CON); // configure the notification type to CONs
	}

	public static DynamicDeviceSemantics getInstance() {

		if(instance == null){
			synchronized (DynamicDeviceSemantics.class){
				if(instance==null){
					instance = new DynamicDeviceSemantics();
				}
			}
		}
		return instance;
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		StringWriter writer = new StringWriter();
		try {
			InputStream in_s = AudioDock.context.getResources().openRawResource(R.raw.audiodock_state);

			byte[] b = new byte[in_s.available()];
			in_s.read(b);
			String theString = new String(b);
			theString = theString.replace("[[POWER_STATE]]",PowerRelay.getRelay()?":on":":off");
			theString = theString.replace("[[INPUT_STATE]]", "\""+AudioInput.getInput()+"\"");
			String song = AudioPlaying.player.getSong();
			String song_state ="";
			if(song !=null &&  AudioPlaying.player.getState().equals("play")) {
				song_state = "\n" +
						"local:song a dbpedia:Song;\n" +
						"       :name \""+song+"\".\n" +
						"       \n" +
						"local:state :song local:song.\n";

			}
			song_state += "local:state  :state :"+  AudioPlaying.player.getState() +".";
			theString = theString.replace("[[SONG_STATE]]", song_state);
			exchange.respond(theString);
		} catch (IOException e) {
			exchange.reject();
		}
	}
}
