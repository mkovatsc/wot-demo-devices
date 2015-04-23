package ch.ethz.inf.vs.wot.demo.devices.utils;

import java.awt.Color;

import javax.swing.*;

@SuppressWarnings("serial")
public class DeviceFrame extends JFrame {

	public DeviceFrame(DevicePanel panel) {
		this.addMouseListener(panel);
		this.addMouseMotionListener(panel);
		this.getContentPane().add(panel);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setUndecorated(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setBackground(new Color(0, 0, 0, 0));
	}
}
