package ch.ethz.inf.vs.wot.demo.services;

import ch.ethz.inf.vs.wot.demo.services.resources.CelsiusToFahrenheit;
import ch.ethz.inf.vs.wot.demo.services.resources.DeviceSemantics;

import ch.ethz.inf.vs.wot.demo.services.resources.FahrenheitToCelsius;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.server.resources.Resource;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The class ConverterServer
 */
public class Converter extends CoapServer {

	private static final String DEMO_IP = "localhost";
	// since we register with the RD, we can use a random port
	private static int port = 0;
	private boolean registed = false;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;
	private String id = UUID.randomUUID().toString();

	public static void main(String[] args) {

		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}

		Converter server = new Converter(port);
		server.start();

		System.out.printf(Converter.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
	}

	public Converter(int... ports) {
		super(ports);
		// add resources to the server
		add(new DeviceSemantics());
		add(new CelsiusToFahrenheit());
		add(new FahrenheitToCelsius());
		new ScheduledThreadPoolExecutor(1).schedule(new Runnable() {
			@Override
			public void run() {
				registerSelf();
			}
		}, 3, TimeUnit.SECONDS);
	}

	private void registerSelf() {
		CoapClient c = createClient();
		c.setTimeout(5000);
		c.setURI("coap://" + DEMO_IP + ":5683");
		Set<WebLink> resources = c.discover("rt=core.rd");
		if (resources != null) {
			if (resources.size() > 0) {
				WebLink w = resources.iterator().next();
				String uri = "coap://" + DEMO_IP + ":5683" + w.getURI();
				registerSelf(uri);
			}
		} else {

			registed = false;
			System.out.println("Discover timeout");
		}
	}

	private void registerSelf(String uri) {
		CoapClient client = createClient();
		client.setTimeout(5000);
		client.setURI(uri + "?ep=" + id);
		client.post(new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				System.out.println("Registered");

			}

			@Override
			public void onError() {
				registed = false;
				System.out.println("Registration Failed");
			}

		}, discoverTree(), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
	}

	String discoverTree() {
		StringBuilder buffer = new StringBuilder();
		for (Resource child : getRoot().getChildren()) {
			LinkFormat.serializeTree(child, null, buffer);
		}
		// remove last comma ',' of the buffer
		if (buffer.length() > 1)
			buffer.delete(buffer.length() - 1, buffer.length());

		return buffer.toString();
	}

	private CoapClient createClient() {
		CoapClient client = new CoapClient();
		List<Endpoint> endpoints = getEndpoints();
		client.setExecutor(getRoot().getExecutor());
		if (!endpoints.isEmpty()) {
			Endpoint ep = endpoints.get(0);
			client.setEndpoint(ep);
		}
		return client;
	}

}