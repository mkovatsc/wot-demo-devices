package ch.ethz.inf.vs.wot.demo.utils.w3c;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.Resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class WoTResource extends CoapResource {
	
	protected static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	protected JsonObject td = new JsonObject();
	
	public WoTResource(String type, String name, String href) {
		super(href);
		this.getAttributes().setTitle(name);
		
		td.addProperty("@type", type);
		td.addProperty("name", name);
	}
	
	@Override
	public void setParent(Resource parent) {
		super.setParent(parent);
		td.addProperty("href", "./"+this.getPath()+this.getName());
	}
}
