package ch.ethz.inf.vs.wot.demo.devices.resources;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.devices.Airconditioner;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class SensorsTemperature extends CoapResource {
	
	private static float temp = 20f;

	public SensorsTemperature() {
		super("temp");
		getAttributes().setTitle("Room Temperature");
		getAttributes().addResourceType("ucum:Cel");
		getAttributes().addInterfaceDescription("core#s");
		getAttributes().setObservable();

		setObservable(true);

		// Set timer task scheduling
		Timer timer = new Timer();
		timer.schedule(new TimeTask(), 1000, 5000);
	}
	
	public static float getTemp() {
		return temp;
	}

	private class TimeTask extends TimerTask {

		@Override
		public void run() {
			
			float prev = Math.round(temp);
			
			int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			// min 18°C, max 28°C, coldest at 3am
			float normal = 23f + (float)Math.cos((hour-15+Math.random())*2f*Math.PI/24f)*5f;
			
			if (PowerRelay.getRelay()) {
				float diff = AcTemperature.getTarget() - temp;
				temp += diff/(200f - 40f*AcVent.getOpened()) + (Math.random()-0.5f); // Takes about 30min for 8°C diff
			} else {
				float diff = normal - temp;
				temp += diff/200f + (Math.random()-0.5f);
			}

			// Call changed to notify subscribers
			if (Math.round(temp)!=prev) changed();
			
			// adjust vent covers
			if (PowerRelay.getRelay()) {
				if (AcVent.getOpened()==0) {
	
					float diff = Math.abs(temp - AcTemperature.getTarget());
					if (diff > 5f) {
						Airconditioner.setVent(1, false);
						Airconditioner.setVent(2, true);
						Airconditioner.setVent(3, false);
					} else if (diff > 2f) {
						Airconditioner.setVent(1, true);
						Airconditioner.setVent(2, false);
						Airconditioner.setVent(3, true);
					} else if (diff < 1f) {
						Airconditioner.setVent(1, true);
						Airconditioner.setVent(2, true);
						Airconditioner.setVent(3, true);
					}
				}
			} else {
				Airconditioner.setVent(1, true);
				Airconditioner.setVent(2, true);
				Airconditioner.setVent(3, true);
			}
			
			Airconditioner.notifyText(new DecimalFormat("Sen 0.0°C").format(temp));
		}
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, new DecimalFormat("0.00").format(temp), TEXT_PLAIN);
	}
}