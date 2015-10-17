package ch.ethz.inf.vs.wot.demo.devices.resources;

import ch.ethz.inf.vs.wot.demo.devices.SmartTV;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class TvAmbient extends CoapResource {
	
	private static Color ambient = new Color(0,0,0);
	
	public static Color getColor() {
		return ambient;
	}

	public TvAmbient() {
		super("ambi");
		getAttributes().setTitle("Ambient color");
		getAttributes().addResourceType("RGB");
		getAttributes().addInterfaceDescription("core#rp");
		getAttributes().setObservable();

		setObservable(true);
		setObserveType(Type.NON);

		// Set timer task scheduling
		Timer timer = new Timer();
		timer.schedule(new TimeTask(), 0, 50);
	}

	private class TimeTask extends TimerTask {

		@Override
		public void run() {
			if (PowerRelay.getRelay()) {
				double phase = (System.currentTimeMillis() % 60000) * 2f * Math.PI / 30000f;
				
				Color temp = Color.getHSBColor((float)TvChannel.getChannel()/312f, ((float)Math.cos(phase)+1f)/2f, ((float)Math.sin(2f*phase)+1f)/2f);

				if (!temp.equals(ambient)) {
					ambient = temp;
					SmartTV.setColor(ambient);
					// Call changed to notify subscribers
					changed();
				}
			} else {
				ambient = new Color(0,0,0);
			}
			
		}
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, String.format("#%02X%02X%02X", ambient.getRed(), ambient.getGreen(), ambient.getBlue()), TEXT_PLAIN);
	}
}
