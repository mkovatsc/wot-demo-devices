package ch.ethz.inf.vs.wot.demo.services.lifx;

import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class LIFXBulb {

    private byte [] address;
    private InetAddress ipBroadcast;
    private String label;
    private int temperature = 3500;

    public LIFXBulb(String macAddress, String network) {
        label = macAddress;
        try {
			ipBroadcast = InetAddress.getByName(network);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        // Convert to big-endian address
        String[] macAddressParts = macAddress.split(":");
        byte[] bigEndianAddress = new byte[8];
        Short byteComponent;
        for(int i=0; i<6; i++) {
            byteComponent = Short.parseShort(macAddressParts[i], 16);
            bigEndianAddress[i] = byteComponent.byteValue();
        }
        bigEndianAddress[6] = (byte) 0x00;
        bigEndianAddress[7] = (byte) 0x00;
        address = bigEndianAddress;

//        // Convert to little-endian address
//        ByteBuffer bigEndianAddressBuffer = ByteBuffer.wrap(bigEndianAddress);
//        ByteBuffer littleEndianAddressBuffer = ByteBuffer.allocate(bigEndianAddress.length);
//        littleEndianAddressBuffer.order( ByteOrder.LITTLE_ENDIAN);
//        int element;
//        while (bigEndianAddressBuffer.hasRemaining()) {
//            element = bigEndianAddressBuffer.getInt();
//            littleEndianAddressBuffer.putInt(element);
//        }
//        address = littleEndianAddressBuffer.array();
    }

    public String toString () { return label; }

    public String getLabel () { return label; }
    
    public void setColor(Color color) {
    	float[] hsv = new float[3];
    	Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);
    	setColor(hsv[0], hsv[1], hsv[2], temperature, 0);
    }

    public void setColor(float hue, float saturation, float brightness, int kelvin, int delay) {
        try {
            byte [] messageData = new LIFXSetColorRequest(address, delay, hue, saturation, brightness, kelvin).generatePacket();
            StringBuilder sb = new StringBuilder();
            for (byte byteValue : messageData) {
                sb.append(String.format("%02x ", byteValue));
            }
            String message = sb.toString();
            send(new Message(messageData, ipBroadcast, 56700));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPower(boolean power, int delay) {
        try {
            byte [] messageData = new LIFXSetPowerRequest(address, delay, power).generatePacket();
            StringBuilder sb = new StringBuilder();
            for (byte byteValue : messageData) {
                sb.append(String.format("%02x ", byteValue));
            }
            String message = sb.toString();
            send(new Message(messageData, ipBroadcast, 56700));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getPower() {
        try {
            byte [] messageData = new LIFXGetPowerRequest(address, 0).generatePacket();
            StringBuilder sb = new StringBuilder();
            for (byte byteValue : messageData) {
                sb.append(String.format("%02x ", byteValue));
            }
            String message = sb.toString();
            send(new Message(messageData, ipBroadcast, 56700));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Message message) {
    	
    	final Message toSend = message;
    	
    	new Thread(new Runnable() {
			public void run() {
				DatagramSocket dataGramSocket = null;	    	
    	        try {
    	            dataGramSocket = new DatagramSocket();
    	            dataGramSocket.setBroadcast(true);
    	            dataGramSocket.setReuseAddress(true);
    	            
    	            DatagramPacket udpPacket = new DatagramPacket(toSend.getMessageData(),
    	            		toSend.getMessageData().length, toSend.getIpAddress(), toSend.getPort());
    	            if (!dataGramSocket.isClosed()) {
    	                dataGramSocket.send(udpPacket);
    	            }
    	        }
    	        catch (Exception e) {
    	            e.printStackTrace();
    	        }
    	        finally {
    	            dataGramSocket.close();
    	        }
			}
		}).start();
	}
}
