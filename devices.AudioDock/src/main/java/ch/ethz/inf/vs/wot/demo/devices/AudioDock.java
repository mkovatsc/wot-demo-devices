package ch.ethz.inf.vs.wot.demo.devices;

import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceManufacturer;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceModel;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceName;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceSemantics;
import ch.ethz.inf.vs.wot.demo.devices.resources.DeviceSerial;
import ch.ethz.inf.vs.wot.demo.devices.resources.PowerCumulative;
import ch.ethz.inf.vs.wot.demo.devices.resources.PowerInstantaneous;
import ch.ethz.inf.vs.wot.demo.devices.resources.PowerRelay;
import ch.ethz.inf.vs.wot.demo.devices.resources.AudioInput;
import ch.ethz.inf.vs.wot.demo.devices.resources.AudioVolume;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * The class ThermostatServer a sample thermostat
 */
public class AudioDock extends CoapServer {

	private static final String DEMO_IP = "localhost";
	private static int port = 0; // since we register with the RD, we can use a random port
	private static final long NOTIFICATION_DELAY = 3; // seconds
	
	private static DevicePanel audio;
	private static JTextArea screen = new JTextArea();
	private static SpeakerPanel speaker1, speaker2;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;
	private String id = UUID.randomUUID().toString();

	private static ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);
	private static ScheduledFuture<?> notifyHandle = null;
	
	public static void setSpeakers(boolean on) {
		if (on) {
			speaker1.setVisible(true);
			speaker2.setVisible(true);
		} else {
			speaker1.setVisible(false);
			speaker2.setVisible(false);
		}
		audio.repaint();
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
		
		AudioDock server = new AudioDock(port);
		server.start();

		System.out.printf(AudioDock.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
	}

	@SuppressWarnings("serial")
	public AudioDock(int... ports) {
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
		add(new CoapResource("audio").add(		
			new AudioVolume(),
			new AudioInput()));		

		// GUI
		audio = new DevicePanel(getClass().getResourceAsStream("audio_640.png"), 640, 287) {
		    @Override
		    protected void paintComponent(Graphics g) {
		        super.paintComponent(g);
		        g.drawImage(super.img, 0, 0, getWidth(), getHeight(), this);

		        if (PowerRelay.getRelay()) {
		        
			        Graphics2D g2 = (Graphics2D) g;
			        
			        Color[] gradient = { new Color(1f, 0.25f,0, 0.9f), new Color(1f, 0.25f ,0, 0.9f), transparent };
			        float[] fraction = { 0.0f, 0.1f, 1.0f };
			        RadialGradientPaint p = new RadialGradientPaint(140, 20, 40, fraction, gradient);
			        g2.setPaint(p);
			        g2.fillOval(40, 0, 200, 33);
		        }
		    }
		};
		speaker1 = new SpeakerPanel();
		speaker1.setBounds(87, 100, 123, 123);
		speaker2 = new SpeakerPanel();
		speaker2.setBounds(428, 100, 123, 123);
		speaker1.setVisible(false);
		speaker2.setVisible(false);
		audio.add(speaker1);
		audio.add(speaker2);

		new DeviceFrame(audio).setVisible(true);
		
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