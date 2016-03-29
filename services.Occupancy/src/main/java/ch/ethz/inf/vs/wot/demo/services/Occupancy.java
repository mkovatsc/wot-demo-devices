package ch.ethz.inf.vs.wot.demo.services;

import ch.ethz.inf.vs.semantics.parser.ExecutionPlan;
import ch.ethz.inf.vs.wot.demo.devices.utils.DeviceServer;
import ch.ethz.inf.vs.wot.demo.services.CoapRequest.Factory;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The class ConverterServer
 */
public class Occupancy implements CoapHandler {

	private static String resourcesDirectory;
	private String reasonerMashupInterface;

	private static String rdLookup = "";

	private static Occupancy instance = new Occupancy();
	AtomicBoolean running = new AtomicBoolean();
	private HashMap<InetAddress, Integer> states;
	
	public Occupancy() {

		states = new HashMap<InetAddress, Integer>();		findResourceDirectories();
	}

	public static void main(String[] args) {


		CoapClient rd = new CoapClient("coap://" + DeviceServer.RD_ADDRESS + ":5683");
		rd.setEndpoint(new CoapEndpoint(new InetSocketAddress("2001:0470:cafe::38b2:cf50",0)));
		Set<WebLink> resources = rd.discover("rt=core.rd-lookup");
		if ((resources != null) && (resources.size() > 0)) {
			
			// set RD lookup URI
			WebLink w = resources.iterator().next();
			rdLookup = "coap://" + DeviceServer.RD_ADDRESS + ":5683" + w.getURI();
			
			Set<WebLink> pirIn = null;
			
			rd = new CoapClient(rdLookup + "/res?rt=pir");
			CoapResponse pir = rd.get();
			if (pir!=null) pirIn = LinkFormat.parse( pir.getResponseText() );
			
			if (pirIn!=null) {
				
				System.out.println("Found " + pirIn.size());
				
				for (WebLink in:pirIn) {
					
					System.out.println("Observing " + in.getURI());
					
					CoapClient client = new CoapClient(in.getURI());
					client.setEndpoint(new CoapEndpoint(new InetSocketAddress("2001:0470:cafe::38b2:cf50",0)));
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

	private void findResourceDirectories() {
		if (resourcesDirectory != null) {
			return;
		}
		CoapClient c = new CoapClient();

		c.setEndpoint(new CoapEndpoint(new InetSocketAddress("2001:0470:cafe::38b2:cf50",0)));
		c.setURI("coap://" + DeviceServer.RD_ADDRESS + ":5683");

		Set<WebLink> resources = c.discover("rt=core.rd-lookup");
		if (resources != null) {
			if (resources.size() > 0) {
				WebLink w = resources.iterator().next();
				String uri = "coap://" + DeviceServer.RD_ADDRESS + ":5683" + w.getURI();
				resourcesDirectory = uri;
			}
		}
	}


	private void findReasonerMashupInterface() {
		findResourceDirectories();
		if (resourcesDirectory == null || reasonerMashupInterface != null) {
			return;
		}

		CoapClient client = new CoapClient();
		client.setEndpoint(new CoapEndpoint(new InetSocketAddress("2001:0470:cafe::38b2:cf50",0)));
		client.setURI(resourcesDirectory + "/res?rt=sr-mashup");
		client.setTimeout(10000);
		CoapResponse response = client.get();

		Set<WebLink> resources = Collections.emptySet();
		if (response.getOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_LINK_FORMAT)
			resources = LinkFormat.parse(response.getResponseText());
		if (resources.size() > 0) {
			WebLink w = resources.iterator().next();
			reasonerMashupInterface = w.getURI().replace("localhost",DeviceServer.RD_ADDRESS);
		}
	}
	@Override
	public void onLoad(CoapResponse response) {
		Integer input = Integer.parseInt(response.getResponseText());
		Integer old = states.put(response.advanced().getSource(), input);
		
		if (old!=null && input!=old) {
			System.out.println("Occupancy changed");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executeExecutionPlan();
		}
	}

	private void executeExecutionPlan() {
		String goal= "@prefix : <ex#>.\n" +
                "@prefix local: <local#>.\n" +
                "@prefix e: <http://eulersharp.sourceforge.net/2003/03swap/log-rules#>.\n" +
                "@prefix dbpedia: <http://dbpedia.org/resource/>.\n" +
                "@prefix geonames: <http://www.geonames.org/ontology#>.\n" +
                "@prefix http: <http://www.w3.org/2011/http#>.\n" +
                "@prefix log: <http://www.w3.org/2000/10/swap/log#>.\n" +
                "@prefix st: <http://purl.org/restdesc/states#>.\n" +
                "@prefix ex: <http://example.org/#>.\n" +
                " \n" +
                "{ \n" +
                "  ?mediaplayerPlace a :current_location.\n" +
                "  ?song a :current_song.\n" +
                "  ?state a :current_state.\n" +
                "  ?cl a :current_location.\n" +
                "\t?s a st:State;\n" +
                "\tlog:includes {?mediaplayerPlace :song ?song.?mediaplayerPlace :songState :play.?cl  :songState :stop.}.\n" +
                "}\n" +
                "=>\n" +
                "{   \n" +
                "}.\n" +
                "\n";
		String goal_input = "@prefix : <ex#>.\n" +
                "@prefix local: <local#>.\n" +
                "@prefix e: <http://eulersharp.sourceforge.net/2003/03swap/log-rules#>.\n" +
                "@prefix dbpedia: <http://dbpedia.org/resource/>.\n" +
                "@prefix geonames: <http://www.geonames.org/ontology#>.\n" +
                "@prefix http: <http://www.w3.org/2011/http#>.\n" +
                "@prefix log: <http://www.w3.org/2000/10/swap/log#>.\n" +
                "@prefix st: <http://purl.org/restdesc/states#>.\n" +
                "@prefix ex: <http://example.org/#>.\n" +
                " \n" +
                "{ \n" +
                "?state :song ?song.\n" +
                "  ?state :state :play.\n" +
                "   ?device :hasState ?state.\n" +
                "  ?device geonames:locatedIn ?l.\n" +
                "}\n" +
                "=>\n" +
                "{  \n" +
                "  ?song a :current_song.\n" +
                "  ?state a :current_state.\n" +
                "  ?l a :current_location.\n" +
                "  ?device a :stopable.\n" +
                "}.\n" +
                "\n" +
                "\n" +
                "{ \n" +
                "  ?mediaplayerPlace a :location. \n" +
                "\t?mediaplayerPlace :presence :on.\n" +
                "}\n" +
                "=>\n" +
                "{   \n" +
                "  ?mediaplayerPlace  a :current_location.\n" +
                "}.\n" +
                "\n" +
                "{\n" +
                "  ?mediaplayerPlace  a :current_location.\n" +
                "  ?p geonames:locatedIn    ?mediaplayerPlace.\n" +
                "}=>{\n" +
                "  ?p  a :changable.\n" +
                "}.\n";

		String  query= goal + "\n########################\n" + goal_input;
		findReasonerMashupInterface();
		if (reasonerMashupInterface != null) {
            CoapClient client = new CoapClient();

			client.setEndpoint(new CoapEndpoint(new InetSocketAddress("2001:0470:cafe::38b2:cf50",0)));
            client.setTimeout(10000);
            client.setURI(reasonerMashupInterface);
            CoapResponse resp = client.post(query, MediaTypeRegistry.TEXT_PLAIN);
            String strplan = resp.getResponseText();
			if(strplan!=null&&!strplan.isEmpty() && running.compareAndSet(false,true)) {
				System.out.println("Plan found");
				final ExecutionPlan plan = new ExecutionPlan(strplan, new Factory());
				plan.execute(new ExecutionPlan.RequestCallback() {
					@Override
					public void onComplete(Object r) {
						boolean done = true;
						for (ExecutionPlan.Request request: plan.getRequests().values()){
							done = done&&request.isDone();

						}
						if(done){
							running.set(false);

							System.out.println("DONE");
						}
					}
				});
			}

        }
	}

	@Override
	public void onError() {
		System.out.println("Observe failed");
	}
}