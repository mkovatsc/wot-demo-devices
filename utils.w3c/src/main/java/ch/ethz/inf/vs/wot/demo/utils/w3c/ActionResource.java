package ch.ethz.inf.vs.wot.demo.utils.w3c;

import java.util.ArrayList;

import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_JSON;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

import com.google.gson.JsonObject;

public class ActionResource extends WoTResource {
	
	public ActionResource(String type, String name, String href) {
		super(type, name, href);
		
		td.addProperty("@type", type);
		td.addProperty("name", name);
	}
	
	public ActionResource(String type, String name, String href, JsonObject inputData) {
		super(type, name, href);
		
		td.addProperty("@type", type);
		td.addProperty("name", name);
		td.add("inputData", inputData);
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {

		ArrayList<String> childPaths = new ArrayList<>();
		for(Resource child : this.getChildren()){
			childPaths.add(child.getURI());
		}
		String response = gson.toJson(childPaths);
		exchange.respond(CONTENT, response, APPLICATION_JSON);
	}
}
