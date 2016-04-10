package ch.ethz.inf.vs.wot.demo.w3c;

import ch.ethz.inf.vs.wot.demo.utils.devices.DeviceFrame;
import ch.ethz.inf.vs.wot.demo.utils.devices.DevicePanel;
import ch.ethz.inf.vs.wot.demo.utils.devices.DeviceServer;
import ch.ethz.inf.vs.wot.demo.utils.w3c.TDRes;
import ch.ethz.inf.vs.wot.demo.w3c.resources.*;

import org.eclipse.californium.core.CoapResource;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The class ThermostatServer a sample thermostat
 */
public class Lightbulb extends DeviceServer {
	
	private static final String DEV_ADDRESS = "0.0.0.0";
	private static final String LIGHTBULB_NAME = "LED Superstar";
	
	private static DevicePanel led;
	private static Color color = Color.white;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;
	
	public static ExecutorService tasks = Executors.newSingleThreadExecutor();

	public static Color getColor() {
		return color;
	}
	
	public static void setColor(Color c) {
		color = c;
		led.repaint();
	}
	
	public static void update() {
		led.repaint();
	}
	
	public static void main(String[] args) {
		
		String address = "0.0.0.0"; // wildcard address -- makes trouble with multiple interfaces
		int port = 0; // since we register with the RD, we can use a random port
		String name = LIGHTBULB_NAME;
		
		if (args.length == 1 && args[0].matches("[0-9]{1,5}")) {
			port = Integer.parseInt(args[0]);
		} else if (args.length > 0) {
			int index = 0;
			while (index < args.length) {
				String arg = args[index];
				if ("-usage".equals(arg) || "-help".equals(arg) || "-h".equals(arg) || "-?".equals(arg)) {
					printUsage();
				} else if ("-a".equals(arg)) {
					address = args[index+1];
				} else if ("-p".equals(arg)) {
					port = Integer.parseInt(args[index+1]);
				} else if ("-n".equals(arg)) {
					name = args[index+1];
				} else {
					System.err.println("Unknwon arg "+arg);
					printUsage();
				}
				index += 2;
			}
		}
		
		Lightbulb server = new Lightbulb(address, port, name);
		server.start();

		System.out.printf(Lightbulb.class.getSimpleName() + " listening on port %d.\n", server.getEndpoints().get(0).getAddress().getPort());
	}

	private static void printUsage() {
		System.out.println();
		System.out.println("SYNOPSIS");
		System.out.println("	" + Lightbulb.class.getSimpleName() + " [-a ADDRESS] [-p PORT] [-n THING_NAME]");
		System.out.println("OPTIONS");
		System.out.println("	-a ADDRESS");
		System.out.println("		Bind the server to a specific host IP address given by ADDRESS (default is wildcard address).");
		System.out.println("	-p PORT");
		System.out.println("		Listen on UDP port PORT (default is random port).");
		System.out.println("	-t THING_NAME");
		System.out.println("		Give a name for the Thing Description metadata.");
		System.exit(0);
	}

	public Lightbulb(int port) {
		this(DEV_ADDRESS, port);
	}
	
	public Lightbulb(String address, int port) {
		this(address, port, LIGHTBULB_NAME);
	}
	
	@SuppressWarnings("serial")
	public Lightbulb(String address, int port, String name) {
		super(address, port);
		// add resources to the server
		getRoot().getChild(".well-known").add(new TDRes(this, name));
		
		add(new CoapResource("dev").add(
			new DeviceManufacturer(),
			new DeviceModel(),
			new DeviceSerial(),
			new DeviceName()));
		add(new CoapResource("pwr").add(
			new PowerInstantaneous(),
			new PowerCumulative(),
			new PowerRelay(),
			new PowerToggle(),
			new ActionOn(),
			new ActionOff()));
		add(new CoapResource("led").add(
			new LEDColor(),
			new LEDObserve(),
			new ActionFade(),
			new ActionBlink()));

		// GUI
		led = new DevicePanel(getClass().getResourceAsStream("superstar_400.png"), 240, 400) {
		    @Override
		    protected void paintComponent(Graphics g) {
		        super.paintComponent(g);
		        
		        if (PowerRelay.getRelay()) {
		        
			        Graphics2D g2 = (Graphics2D) g;
			        
			        Color[] gradient = { new Color(color.getRed(), color.getGreen(), color.getBlue(), 240*color.getAlpha()/255),
			        					 new Color(color.getRed(), color.getGreen(), color.getBlue(), 200*color.getAlpha()/255),
			        					 new Color(color.getRed(), color.getGreen(), color.getBlue(), 0) };
			        float[] fraction = { 0.0f, 0.5f, 1.0f };
			        RadialGradientPaint p = new RadialGradientPaint(120, 120, 120, fraction, gradient);
			        g2.setPaint(p);
			        g2.fillOval(0, 0, 240, 240);
		        }
		    }
		};

		new DeviceFrame(led).setVisible(true);
	}
}