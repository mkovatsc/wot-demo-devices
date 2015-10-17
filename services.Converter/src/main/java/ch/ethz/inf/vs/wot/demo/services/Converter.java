package ch.ethz.inf.vs.wot.demo.services;

import ch.ethz.inf.vs.wot.demo.devices.utils.DeviceServer;
import ch.ethz.inf.vs.wot.demo.services.resources.CelsiusToFahrenheit;
import ch.ethz.inf.vs.wot.demo.services.resources.DeviceSemantics;
import ch.ethz.inf.vs.wot.demo.services.resources.FahrenheitToCelsius;

/**
 * The class ConverterServer
 */
public class Converter extends DeviceServer {

	// since we register with the RD, we can use a random port
	private static int port = 0;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;

	public static void main(String[] args) {

		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}

		Converter server = new Converter(port);
		server.start();

		System.out.printf(Converter.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
	}

	public Converter(int port) {
		super(port);
		// add resources to the server
		add(new DeviceSemantics());
		add(new CelsiusToFahrenheit());
		add(new FahrenheitToCelsius());
	}

}