package ch.ethz.inf.vs.wot.demo.utils.devices;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


@SuppressWarnings("serial")
public class DevicePanel extends JPanel implements MouseListener, MouseMotionListener {

	public static final Color transparent = new Color(0,0,0, Color.TRANSLUCENT);
	
    protected BufferedImage img;
    
    private int width;
    private int height;

    public DevicePanel(InputStream imageFile, int width, int height) {
        
    	this.width = width;
    	this.height = height;
    	
    	setOpaque(false); // transparent
        setLayout(null); // absolute layout
        
        try {
            img = ImageIO.read(imageFile);
        } catch (IOException ex) {
            Logger.getLogger(DevicePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
    }

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(this.width, this.height);
	}

	private Point start;

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		start = e.getPoint();
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		Point p = e.getLocationOnScreen();
		Component c = e.getComponent();
		c.setLocation((int) (p.getX() - start.getX()), (int) (p.getY() - start.getY()));
		c.repaint();
	}

	public void mouseMoved(MouseEvent e) {
	}
}

