package ch.ethz.inf.vs.wot.demo.utils.w3c;

import com.google.gson.JsonObject;

public class EventResource extends ActionResource {

	public EventResource(String type, String name, String href, JsonObject inputData) {
		super(type, name, href, inputData);
	}

}
