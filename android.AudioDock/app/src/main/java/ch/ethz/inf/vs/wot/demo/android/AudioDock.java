package ch.ethz.inf.vs.wot.demo.android;

import android.content.Context;

import org.eclipse.californium.core.CoapResource;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import ch.ethz.inf.vs.wot.demo.android.resources.AudioInput;
import ch.ethz.inf.vs.wot.demo.android.resources.AudioNow;
import ch.ethz.inf.vs.wot.demo.android.resources.AudioPlaying;
import ch.ethz.inf.vs.wot.demo.android.resources.DeviceManufacturer;
import ch.ethz.inf.vs.wot.demo.android.resources.DeviceModel;
import ch.ethz.inf.vs.wot.demo.android.resources.DeviceName;
import ch.ethz.inf.vs.wot.demo.android.resources.DeviceSemantics;
import ch.ethz.inf.vs.wot.demo.android.resources.DeviceSerial;
import ch.ethz.inf.vs.wot.demo.android.resources.DynamicDeviceSemantics;
import ch.ethz.inf.vs.wot.demo.android.resources.PowerCumulative;
import ch.ethz.inf.vs.wot.demo.android.resources.PowerInstantaneous;
import ch.ethz.inf.vs.wot.demo.android.resources.PowerRelay;

/**
 * The class ThermostatServer a sample thermostat
 */
public class AudioDock extends DeviceServer {
	public static Context context;
	private static ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);
	private static int port = 0; // since we register with the RD, we can use a random port
	private static final long NOTIFICATION_DELAY = 3; // seconds

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;

	private static ScheduledFuture<?> notifyHandle = null;


	public static void main(String[] args) {
		
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		
		AudioDock server = new AudioDock(port);
		server.start();

		System.out.printf(AudioDock.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
	}

	@SuppressWarnings("serial")
	public AudioDock(int port) {
		super(port);
		// add resources to the server
		add(new DeviceSemantics());
		add(DynamicDeviceSemantics.getInstance());
		add(new CoapResource("dev").add(
			new DeviceManufacturer(),
			new DeviceModel(),
			new DeviceSerial(),
			new DeviceName()));
		add(new CoapResource("pwr").add(
				new PowerInstantaneous(),
				new PowerCumulative(),
				new PowerRelay()));
		add(new CoapResource("audio").add(
				new AudioPlaying(),
				new AudioNow(),
				new AudioInput()));

	}

	public static void setSpeakers(boolean speakers) {

	}

	public static void setLight(boolean light) {
	}
}