package ch.ethz.inf.vs.wot.demo.services;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.Set;

/**
 * The class ConverterServer
 */
public class Ambient {

	private static final String DEMO_IP = "localhost";

	private static String rdLookup = "";

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;

	public static void main(String[] args) {
		CoapClient rd = new CoapClient("coap://" + DEMO_IP + ":5683");
		Set<WebLink> resources = rd.discover("rt=core.rd-lookup");
		if ((resources != null) && (resources.size() > 0)) {
			
			// set RD lookup URI
			WebLink w = resources.iterator().next();
			rdLookup = "coap://" + DEMO_IP + ":5683" + w.getURI();
			
			WebLink rgbOut = null;
			Set<WebLink> rgbIns = null;
			
			rd = new CoapClient(rdLookup + "/res?rt=RGB");
			CoapResponse rgb = rd.get();
			if (rgb!=null) rgbOut = LinkFormat.parse( rgb.getResponseText() ).iterator().next();
				
			rd = new CoapClient(rdLookup + "/res?rt=led:obs");
			CoapResponse obs = rd.get();
			if (obs!=null) rgbIns = LinkFormat.parse( obs.getResponseText() );
			
			if (rgbOut!=null && rgbIns!=null) {
				
				System.out.println("Found " + rgbIns.size());
				
				for (WebLink in:rgbIns) {
					
					System.out.println("Setting " + in.getURI());
					
					CoapClient client = new CoapClient(in.getURI());
					client.put(rgbOut.getURI(), MediaTypeRegistry.TEXT_PLAIN);
					
					switchDevice(in);
				}
			}
			
			switchDevice(rgbOut);
			
		} else {
			System.out.println("No RD found");
		}
	}
	
	private static void switchDevice(WebLink link) {
		CoapClient rd = new CoapClient(rdLookup + "/res?ep=" + link.getAttributes().getAttributeValues("ep").get(0) + "&rt=switch");
		CoapResponse res = rd.get();
		if (res!=null) {
			WebLink on = LinkFormat.parse( res.getResponseText() ).iterator().next();
			CoapClient device = new CoapClient(on.getURI());
			device.put("1", MediaTypeRegistry.TEXT_PLAIN);
		}
	}
}