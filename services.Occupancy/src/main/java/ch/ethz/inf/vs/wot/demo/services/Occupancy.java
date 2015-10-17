package ch.ethz.inf.vs.wot.demo.services;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.LinkFormat;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Set;

/**
 * The class ConverterServer
 */
public class Occupancy implements CoapHandler {

	private static final String DEMO_IP = "[2001:0470:cafe::38b2:cf50]";

	private static String rdLookup = "";

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;
	
	private static Occupancy instance = new Occupancy();
	
	private HashMap<InetAddress, Integer> states;
	
	public Occupancy() {
		states = new HashMap<InetAddress, Integer>();
	}

	public static void main(String[] args) {
		CoapClient rd = new CoapClient("coap://" + DEMO_IP + ":5683");
		Set<WebLink> resources = rd.discover("rt=core.rd-lookup");
		if ((resources != null) && (resources.size() > 0)) {
			
			// set RD lookup URI
			WebLink w = resources.iterator().next();
			rdLookup = "coap://" + DEMO_IP + ":5683" + w.getURI();
			
			Set<WebLink> pirIn = null;
			
			rd = new CoapClient(rdLookup + "/res?rt=pir");
			CoapResponse pir = rd.get();
			if (pir!=null) pirIn = LinkFormat.parse( pir.getResponseText() );
			
			if (pirIn!=null) {
				
				System.out.println("Found " + pirIn.size());
				
				for (WebLink in:pirIn) {
					
					System.out.println("Observing " + in.getURI());
					
					CoapClient client = new CoapClient(in.getURI());
					client.observe(instance);
				}
				
				try {
					// keep the program running
					System.in.read();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		} else {
			System.out.println("No RD found");
		}
	}

	@Override
	public void onLoad(CoapResponse response) {
		Integer input = Integer.parseInt(response.getResponseText());
		Integer old = states.put(response.advanced().getSource(), input);
		
		if (old!=null && input!=old) {
			System.out.println("Occupancy changed");
			// TODO send goal to reasoning server
			
			// TODO execute execution plan
		}
	}

	@Override
	public void onError() {
		System.out.println("Observe failed");
	}
}