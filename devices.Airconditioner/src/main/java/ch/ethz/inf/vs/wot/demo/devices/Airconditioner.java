package ch.ethz.inf.vs.wot.demo.devices;

import ch.ethz.inf.vs.wot.demo.devices.resources.*;
import ch.ethz.inf.vs.wot.demo.devices.utils.DeviceFrame;
import ch.ethz.inf.vs.wot.demo.devices.utils.DevicePanel;
import ch.ethz.inf.vs.wot.demo.devices.utils.DeviceServer;
import org.eclipse.californium.core.CoapResource;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The class ThermostatServer a sample thermostat
 */
public class Airconditioner extends DeviceServer {

	private static int port = 0; // since we register with the RD, we can use a random port
	private static final long NOTIFICATION_DELAY = 3; // seconds
	
	private static DevicePanel ac;
	private static JTextArea screen = new JTextArea();
	private static VentPanel vent1, vent2, vent3;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;

	private static ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);
	private static ScheduledFuture<?> notifyHandle = null;
	
	/**
	 * 
	 * @param index the vent
	 * @param mode true shows cover, false shows open vent
	 */
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

	public Airconditioner(int port) {
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