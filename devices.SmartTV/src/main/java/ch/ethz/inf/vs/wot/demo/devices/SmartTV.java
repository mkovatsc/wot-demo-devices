package ch.ethz.inf.vs.wot.demo.devices;

import ch.ethz.inf.vs.wot.demo.devices.resources.*;
import ch.ethz.inf.vs.wot.demo.utils.devices.DeviceFrame;
import ch.ethz.inf.vs.wot.demo.utils.devices.DevicePanel;
import ch.ethz.inf.vs.wot.demo.utils.devices.DeviceServer;

import org.eclipse.californium.core.CoapResource;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The class ThermostatServer a sample thermostat
 */
public class SmartTV extends DeviceServer {

	// since we register with the RD, we can use a random port
	private static int port = 0;
	private static final long NOTIFICATION_DELAY = 3; // seconds
	
	private static final Color transparent = new Color(0,0,0, Color.TRANSLUCENT);
	private static DevicePanel tv;
	private static JTextArea screen = new JTextArea(80, 25);
	
	private static ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);
	private static ScheduledFuture<?> notifyHandle = null;

	// exit codes for runtime errors
	public static final int ERR_INIT_FAILED = 1;
	
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

	public SmartTV(int port) {
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
	}

}