package ch.ethz.inf.vs.wot.demo.w3c.resources;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

import ch.ethz.inf.vs.wot.demo.w3c.resources.WoTResource.Interaction;

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
			
			for (Resource r : thing.getRoot().getChildren()) {
				for (Resource res : r.getChildren()) {
					if (res instanceof WoTResource) {
						JsonArray hrefs = new JsonArray();
						hrefs.add(new JsonPrimitive(res.getPath()+res.getName()));
						((WoTResource) res).td.add("href", hrefs);
						
						if (Interaction.PROPERTY.equals(((WoTResource) res).interaction)) {
							this.properties.add(((WoTResource) res).td);
						} else if (Interaction.ACTION.equals(((WoTResource) res).interaction)) {
							this.actions.add(((WoTResource) res).td);
						} else if (Interaction.EVENT.equals(((WoTResource) res).interaction)) {
							this.events.add(((WoTResource) res).td);
						}
					}
				}
			}
			
			return gson.toJson(this);
		}
	}
}
