package ch.ethz.inf.vs.wot.demo.services;

import ch.ethz.inf.vs.wot.demo.devices.utils.DeviceServer;
import ch.ethz.inf.vs.wot.demo.services.lifx.LIFXBulb;
import ch.ethz.inf.vs.wot.demo.services.resources.*;
import org.eclipse.californium.core.CoapResource;


public class LIFX extends DeviceServer {

	public static final String[] LIFX_BULB_MAC = new String[]{"D0:73:D5:00:CC:57", "D0:73:D5:00:30:EE"};
	public static final String WIFI_BROADCAST_ADDRESS = "192.168.1.255";

	// since we register with the RD, we can use a random port
	private static int port = 0;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;

	public final LIFXBulb bulb;

	public static void main(String[] args) {
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		for(String mac:LIFX_BULB_MAC) {
			LIFX server = new LIFX(port, mac);
			server.start();
			System.out.printf(LIFX.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
		}

	}
	

	public LIFX(int port, String mac) {
		super(port);
		bulb = new LIFXBulb(mac, WIFI_BROADCAST_ADDRESS);
		// add resources to the server
		add(new DeviceSemantics());
		add(new CoapResource("dev").add(
			new DeviceManufacturer(),
			new DeviceModel(),
			new DeviceSerial(this),
			new DeviceName()));
		add(new CoapResource("pwr").add(
				new PowerInstantaneous(),
				new PowerCumulative(),
				new PowerRelay(this)));
		add(new CoapResource("led").add(
				new LEDColor(this),
				new LEDObserve(this)));
	}

}