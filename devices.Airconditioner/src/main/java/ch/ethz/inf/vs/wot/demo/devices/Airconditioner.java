package ch.ethz.inf.vs.wot.demo.devices;

import ch.ethz.inf.vs.wot.demo.devices.resources.*;
import ch.ethz.inf.vs.wot.demo.devices.utils.DeviceFrame;
import ch.ethz.inf.vs.wot.demo.devices.utils.DevicePanel;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.server.resources.Resource;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * The class ThermostatServer a sample thermostat
 */
public class Airconditioner extends CoapServer {

	private static final String DEMO_IP = "localhost";
	private static int port = 0; // since we register with the RD, we can use a random port
	private static final long NOTIFICATION_DELAY = 3; // seconds
	
	private static DevicePanel ac;
	private static JTextArea screen = new JTextArea();
	private static VentPanel vent1, vent2, vent3;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;
	private String id = UUID.randomUUID().toString();

	private static ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);
	private static ScheduledFuture<?> notifyHandle = null;
	
	public static void setVent(int index, boolean mode) {
		switch (index) {
		case 1: vent1.setVisible(mode); vent1.repaint(); break;
		case 2: vent2.setVisible(mode); vent2.repaint(); break;
		case 3: vent3.setVisible(mode); vent3.repaint(); break;
		}
		ac.repaint();
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
		
		Airconditioner server = new Airconditioner(port);
		server.start();

		System.out.printf(Airconditioner.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
	}

	public Airconditioner(int... ports) {
		super(ports);
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
		add(new CoapResource("ac").add(		
			new AcTemperature(),
			new AcVent()));
		add(new CoapResource("sen").add(
			new SensorsTemperature()));

		// GUI
		ac = new DevicePanel(getClass().getResourceAsStream("airconditioner_800.png"), 152, 800);  
        screen.setBounds(42, 3, 68, 14);
		screen.setBackground(Color.darkGray);
		screen.setForeground(Color.cyan);
		screen.setFont(new Font("Sans Serif", Font.CENTER_BASELINE, 12));
		screen.setEditable(false);
		ac.add(screen);
		vent1 = new VentPanel();
		vent1.setBounds(15, 65, 122, 122);
		vent2 = new VentPanel();
		vent2.setBounds(15, 192, 122, 122);
		vent3 = new VentPanel();
		vent3.setBounds(15, 317, 122, 122);
		ac.add(vent1);
		ac.add(vent2);
		ac.add(vent3);
		
		new DeviceFrame(ac).setVisible(true);
		
		tasks.schedule(new Runnable() {
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
			notifyText("Discover timeout");
		}
	}

	private void registerSelf(String uri) {
		CoapClient client = createClient();
		client.setTimeout(5000);
		client.setURI(uri + "?ep=" + id);
		client.post(new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				notifyText("Registered");

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

	
	@SuppressWarnings("serial")
	public class VentPanel extends JPanel {

		private BufferedImage image;

		public VentPanel() {
			try {
				image = ImageIO.read(getClass().getResourceAsStream("vent_800.png"));
			} catch (IOException ex) {
				// handle exception...
			}
			setOpaque(false); // transparent
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(image, 0, 0, null); // see javadoc for more info on the
											// parameters
		}

	}
}