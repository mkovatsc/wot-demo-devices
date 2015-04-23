package ch.ethz.inf.vs.wot.demo.devices;

import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceManufacturer;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceModel;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceName;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceSemantics;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceSerial;
import ch.ethz.inf.vs.wot.demo.devices.resources.LEDObserve;
import ch.ethz.inf.vs.wot.demo.devices.resources.PowerCumulative;
import ch.ethz.inf.vs.wot.demo.devices.resources.PowerInstantaneous;
import ch.ethz.inf.vs.wot.demo.devices.resources.PowerRelay;
import ch.ethz.inf.vs.wot.demo.devices.resources.LEDColor;
import ch.ethz.inf.vs.wot.demo.devices.utils.DeviceFrame;
import ch.ethz.inf.vs.wot.demo.devices.utils.DevicePanel;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.server.resources.Resource;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * The class ThermostatServer a sample thermostat
 */
public class Lightbulb extends CoapServer {

	private static final String DEMO_IP = "localhost";
	private static int port = 0; // since we register with the RD, we can use a random port
	private static final int RD_LIFETIME = 60; // minimum is 60 seconds
	
	private static DevicePanel led;
	private static Color color = Color.white;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;
	private String id = UUID.randomUUID().toString();
	private String rdHandle;

	private static ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);
	
	public static void setColor(Color c) {
		color = c;
		led.repaint();
	}
	
	public static void update() {
		led.repaint();
	}
	
	public static void main(String[] args) {
		
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		
		Lightbulb server = new Lightbulb(port);
		server.start();

		System.out.printf(Lightbulb.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
	}

	@SuppressWarnings("serial")
	public Lightbulb(int... ports) {
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
		add(new CoapResource("led").add(
			new LEDColor(),
			new LEDObserve()));		

		// GUI
		led = new DevicePanel(getClass().getResourceAsStream("candle_400.png"), 240, 400) {
		    @Override
		    protected void paintComponent(Graphics g) {
		        super.paintComponent(g);
		        g.drawImage(super.img, 0, 0, getWidth(), getHeight(), this);

		        if (PowerRelay.getRelay()) {
		        
			        Graphics2D g2 = (Graphics2D) g;
			        
			        Color[] gradient = { new Color(color.getRed(), color.getGreen(), color.getBlue(), 240), new Color(color.getRed(), color.getGreen(), color.getBlue(), 240), transparent };
			        float[] fraction = { 0.0f, 0.2f, 1.0f };
			        RadialGradientPaint p = new RadialGradientPaint(120, 120, 120, fraction, gradient);
			        g2.setPaint(p);
			        g2.fillOval(0, 0, 240, 240);
		        }
		    }
		};

		new DeviceFrame(led).setVisible(true);
		
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
			System.out.println("Discover timeout");
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
				
				tasks.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						client.setURI(rdHandle);
						client.post("", MediaTypeRegistry.APPLICATION_LINK_FORMAT);
					}
				}, RD_LIFETIME, RD_LIFETIME, TimeUnit.SECONDS);
			}

			@Override
			public void onError() {
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
	public class SpeakerPanel extends JPanel {

		private ImageIcon image;

		public SpeakerPanel() {
			image = new ImageIcon(getClass().getResource("speaker.gif"));
			setOpaque(false); // transparent
		    
			JLabel iconLabel = new JLabel();
		    iconLabel.setIcon(image);
		    image.setImageObserver(iconLabel);

		    JLabel label = new JLabel("Loading...");
		    this.add(iconLabel);
		    this.add(label);
		}


	}
}