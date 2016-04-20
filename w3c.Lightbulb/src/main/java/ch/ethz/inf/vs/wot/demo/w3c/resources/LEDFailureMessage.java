package ch.ethz.inf.vs.wot.demo.w3c.resources;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.BAD_REQUEST;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CHANGED;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CREATED;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_JSON;

import java.awt.Color;
import java.util.Date;

import ch.ethz.inf.vs.wot.demo.utils.w3c.ActionResource;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.w3c.Lightbulb;

public class LEDFailureMessage extends ActionResource {
	private static Color color = Color.white;
	private int myPriority = 0;
	private int lastOccuredPriority = 0;
	
	private class AlertMessage{
		public int priority;
		public Date timestamp;
		public boolean acknowledged;
	}

	public LEDFailureMessage(int instance, int priority) {
		super("SimpleAlarm", "Failure", String.format("message_receiver_%d", instance));
		myPriority = priority;
		getAttributes().setObservable();
		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		Date now = new Date();
		exchange.respond(CONTENT, String.format("{\"timestamp\":\"%s\",\"priority\":\"%d\"}", now.toString(), lastOccuredPriority), APPLICATION_JSON);
	}
	
	@Override
	public void handleDELETE(CoapExchange exchange){
		this.delete();
		exchange.respond(ResponseCode.DELETED);
	}
	
	@Override
	public void handlePOST(CoapExchange exchange) {
		
		int prio = 0;
		
		try {
			prio = Integer.parseInt(exchange.getRequestText());
		} catch (NumberFormatException e) {
			// 0
		}
		myPriority = prio;
	}	
	
	public void notifyEvent(int priority){
		lastOccuredPriority = priority;
		if(priority >= myPriority)
			changed();
	}
	
}
