package ch.ethz.inf.vs.wot.demo.devices;

import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceManufacturer;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceModel;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceName;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceSemantics;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceSerial;
import ch.ethz.inf.vs.wot.demo.devices.resources.PowerCumulative;
import ch.ethz.inf.vs.wot.demo.devices.resources.PowerInstantaneous;
import ch.ethz.inf.vs.wot.demo.devices.resources.PowerRelay;
import ch.ethz.inf.vs.wot.demo.devices.resources.TvAmbient;
import ch.ethz.inf.vs.wot.demo.devices.resources.TvChannel;
import ch.ethz.inf.vs.wot.demo.devices.resources.TvInput;
import ch.ethz.inf.vs.wot.demo.devices.resources.TvVolume;
import ch.ethz.inf.vs.wot.demo.devices.utils.DeviceFrame;
import ch.ethz.inf.vs.wot.demo.devices.utils.DevicePanel;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.server.resources.Resource;

import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

/**
 * The class ThermostatServer a sample thermostat
 */
public class SmartTV extends CoapServer {

	private static final String DEMO_IP = "127.0.0.1";
	// since we register with the RD, we can use a random port
	private static int port = 0;
	private static final long NOTIFICATION_DELAY = 3; // seconds
	private static final int RD_LIFETIME = 60; // minimum is 60 seconds
	
	private static final Color transparent = new Color(0,0,0, Color.TRANSLUCENT);
	private static DevicePanel tv;
	private static JTextArea screen = new JTextArea(80, 25);
	
	private static ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);
	private static ScheduledFuture<?> notifyHandle = null;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;
	
	private String id = UUID.randomUUID().toString();
	private String rdHandle;
	
	public static void setColor(Color set) {
		screen.setBackground(set);
	}
	
	public static void setText(String text) {
		screen.setText(text);
	}
	
	public static void notifyText(String text) {
		if (notifyHandle != null) {
			notifyHandle.cancel(true);
		}
		
		screen.setText(text);
		screen.repaint();
		
		notifyHandle = tasks.schedule(new Runnable() {
			@Override
			public void run() {
				screen.setText(null);
				screen.repaint();
			}
		}, NOTIFICATION_DELAY, TimeUnit.SECONDS);
	}

	public static void main(String[] args) {
		
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		
		SmartTV server = new SmartTV(port);
		server.start();

		System.out.printf(SmartTV.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
	}

	public SmartTV(int... ports) {
		super(ports);
		// add resources to the server
		add(new DeviceSemantics());
		add(new CoapResource("dev").add(
			new DeviceManufacturer(),
			new DeviceModel(),
			new DeviceSerial(),
			new DeviceName()));
		add(new CoapResource("pwr").add(
			new PowerInstantaneous(),
			new PowerCumulative(),
			new PowerRelay()));
		add(new CoapResource("tv").add(		
			new TvChannel(),
			new TvVolume(),
			new TvAmbient(),
			new TvInput()));
		
		// GUI
		tv = new DevicePanel(getClass().getResourceAsStream("smarttv_800.png"), 800, 492);  
        screen.setBounds(8, 5, 783, 441);
        screen.setBorder(BorderFactory.createLineBorder(transparent, 10));
		screen.setBackground(Color.black);
		screen.setForeground(Color.green);
		screen.setFont(new Font("Sans Serif", Font.BOLD, 24));
		screen.setEditable(false);
		tv.add(screen);
		new DeviceFrame(tv).setVisible(true);
        
        tasks.schedule(new Runnable() {
			@Override
			public void run() {
				notifyText("Registering at " + DEMO_IP);
				registerSelf();
			}
		}, 2, TimeUnit.SECONDS);
	}

	private void registerSelf() {
		CoapClient c = createClient();
		c.setTimeout(5000);
		c.setURI("coap://" + DEMO_IP + ":5683");
		Set<WebLink> resources = c.discover("rt=core.rd");
		if (resources != null) {
			if (resources.size() > 0) {
				WebLink w = resources.iterator().next();
				registerSelf("coap://" + DEMO_IP + ":5683" + w.getURI());
			}
		} else {
			notifyText("Discover timeout");
		}
	}

	private void registerSelf(String uri) {
		final CoapClient client = createClient();
		client.setTimeout(5000);
		client.setURI(uri + "?ep=" + id + "&lt=" + RD_LIFETIME);
		client.post(new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				
				rdHandle = "coap://" + DEMO_IP + ":5683/" + response.getOptions().getLocationPathString();
				notifyText("Registered: " + rdHandle);
				
				tasks.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						notifyText("RD Update: "+rdHandle);
						client.setURI(rdHandle);
						client.post("", MediaTypeRegistry.APPLICATION_LINK_FORMAT);
					}
				}, RD_LIFETIME, RD_LIFETIME, TimeUnit.SECONDS);
			}

			@Override
			public void onError() {
				notifyText("Registration Failed");
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