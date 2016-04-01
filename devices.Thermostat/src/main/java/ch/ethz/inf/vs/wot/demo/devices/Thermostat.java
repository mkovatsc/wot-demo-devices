package ch.ethz.inf.vs.wot.demo.devices;

import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceSemantics;
import ch.ethz.inf.vs.wot.demo.devices.resources.DynamicDeviceSemantics;
import ch.ethz.inf.vs.wot.demo.devices.resources.Temperature;
import ch.ethz.inf.vs.wot.demo.utils.devices.DeviceServer;

/**
 * The class Thermostat provides a CoAP server and emulates a smart thermostat.
 */
public class Thermostat extends DeviceServer {

	// since we register with the RD, we can use a random port
	private static int port = 0;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;

	public static void main(String[] args) {

		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		// create server 5611
		Thermostat server = new Thermostat(port);
		server.start();

		System.out.printf(Thermostat.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());

	}

	public Thermostat(int port) {
		super(port);
		// add resources to the server
		add(new DeviceSemantics());
		add(new DynamicDeviceSemantics());
		add(new Temperature());
	}

}