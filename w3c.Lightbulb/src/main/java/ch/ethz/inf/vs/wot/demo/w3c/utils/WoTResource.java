package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.CoapResource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class WoTResource extends CoapResource {
	
	enum Interaction {
		PROPERTY,
		ACTION,
		EVENT
	}
	
	static Gson gson = new  Gson();
	
	public Interaction interaction;
	
	JsonObject td = new JsonObject();
	
	public WoTResource(Interaction interactionType, String type, String name, String href) {
		super(href);
		this.getAttributes().setTitle(name);
		
		this.interaction = interactionType;
		
		td.addProperty("@type", type);
		td.addProperty("name", name);
		
	}

}
