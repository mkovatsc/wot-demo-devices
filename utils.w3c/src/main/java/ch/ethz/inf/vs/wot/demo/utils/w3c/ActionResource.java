package ch.ethz.inf.vs.wot.demo.utils.w3c;

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
}
