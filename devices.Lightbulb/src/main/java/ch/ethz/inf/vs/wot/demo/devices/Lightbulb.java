package ch.ethz.inf.vs.wot.demo.devices;

import ch.ethz.inf.vs.wot.demo.devices.resources.*;
import ch.ethz.inf.vs.wot.demo.utils.devices.DeviceFrame;
import ch.ethz.inf.vs.wot.demo.utils.devices.DevicePanel;
import ch.ethz.inf.vs.wot.demo.utils.devices.DeviceServer;

import org.eclipse.californium.core.CoapResource;

import javax.swing.*;
import java.awt.*;


/**
 * The class ThermostatServer a sample thermostat
 */
public class Lightbulb extends DeviceServer {

	private static int port = 0; // since we register with the RD, we can use a random port
	
	private static DevicePanel led;
	private static Color color = Color.white;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;
	
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
	public Lightbulb(int port) {
		super(port);
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
			        
			        Color[] gradient = { new Color(color.getRed(), color.getGreen(), color.getBlue(), 240), new Color(color.getRed(), color.getGreen(), color.getBlue(), 200), new Color(color.getRed(), color.getGreen(), color.getBlue(), 0) };
			        float[] fraction = { 0.0f, 0.5f, 1.0f };
			        RadialGradientPaint p = new RadialGradientPaint(120, 120, 120, fraction, gradient);
			        g2.setPaint(p);
			        g2.fillOval(0, 0, 240, 240);
		        }
		    }
		};

		new DeviceFrame(led).setVisible(true);
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