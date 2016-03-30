package ch.ethz.inf.vs.wot.demo.w3c.utils;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_JSON;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class TDRes extends CoapResource {
	
	protected CoapServer thing;
	protected Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public TDRes(CoapServer thing) {
		super("td");
		getAttributes().setTitle("W3C WoT Thing Description");
		this.thing = thing;
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		TD td = new TD();
		
		exchange.respond(CONTENT, td.toString(), APPLICATION_JSON);
	}
	
	private class TD {
		
		@SerializedName("@context")
		private String[] context = {"https://w3c.github.io/wot/w3c-wot-td-context.jsonld", "https://w3c.github.io/wot/w3c-wot-common-context.jsonld"};
		@SerializedName("@type")
		private String type = "Thing";
		
		@SerializedName("name")
		private String name;
		@SerializedName("uris")
		private ArrayList<String> uris;
		@SerializedName("encodings")
		private String[] encodings = {"JSON"};
		
		@SerializedName("properties")
		private ArrayList<JsonObject> properties = new ArrayList<JsonObject>();

		@SerializedName("actions")
		private ArrayList<JsonObject> actions = new ArrayList<JsonObject>();

		@SerializedName("events")
		private ArrayList<JsonObject> events = new ArrayList<JsonObject>();
		
		public String toString() {

			// get base URI
			String uri = "coap://";
			InetAddress addr = thing.getEndpoints().get(0).getAddress().getAddress();
			if (addr instanceof Inet4Address) {
				uri += addr.getHostAddress();
			} else {
				uri += "[" + addr.getHostAddress() + "]";
			}
			uri += ":" + thing.getEndpoints().get(0).getAddress().getPort() + "/";
			this.uris = new ArrayList<String>();
			this.uris.add(uri);
			
			traverseTree(thing.getRoot(), this.properties, this.actions, this.events);

			return gson.toJson(this);
		}
		
		private void traverseTree(Resource start, ArrayList<JsonObject> properties, ArrayList<JsonObject> actions, ArrayList<JsonObject> events) {
			for (Resource res : start.getChildren()) {
				traverseTree(res, properties, actions, events);
				
				if (res instanceof PropertyResource) {
					properties.add(((WoTResource) res).td);
				} else if (res instanceof ActionResource) {
					actions.add(((WoTResource) res).td);
				}
			}
		}
	}
}
