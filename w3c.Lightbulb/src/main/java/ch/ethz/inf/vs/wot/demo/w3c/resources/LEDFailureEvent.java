package ch.ethz.inf.vs.wot.demo.w3c.resources;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CREATED;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

import com.google.gson.JsonObject;

import ch.ethz.inf.vs.wot.demo.utils.w3c.ActionResource;

public class LEDFailureEvent extends ActionResource {
	

	public LEDFailureEvent(){
		super("event", "FaultEvent", "fault", gson.fromJson("{\"valueType\":[null,\"xsd:unsignedInteger\"]}", JsonObject.class));
	}
	
	public void notifyEvent(int priority){
		Collection<Resource> children = this.getChildren();
		
		for(Resource child : children){
			if(child instanceof LEDFailureMessage){
				LEDFailureMessage failureMessage = (LEDFailureMessage)child;
				failureMessage.notifyEvent(priority);
			}
		}
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		
		exchange.respond(CONTENT, this.getChildren().toString(), TEXT_PLAIN);
	}
	
	@Override
	public void handlePOST(CoapExchange exchange) {
		
		int prio = 0;
		
		try {
			prio = Integer.parseInt(exchange.getRequestText());
		} catch (NumberFormatException e) {
			// 0
		}
		
		LEDFailureMessage task = new LEDFailureMessage(this.getChildren().size(), prio);
		
		this.add(task);
		
		exchange.setLocationPath(task.getPath()+task.getName());
		exchange.respond(CREATED);
	}	
}
