package ch.ethz.inf.vs.wot.demo.devices.resources;

import ch.ethz.inf.vs.wot.demo.devices.AudioDock;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MP3Player {

	private Player player;
	private InputStream FIS;
	private BufferedInputStream BIS;
	private boolean canResume;
	private InputStream mp3;
	private int total;
	private int stopped;
	private boolean valid;
	private String song;
	private String state;
	private int lastPosition;

	public MP3Player() {
		player = null;
		FIS = null;
		valid = false;
		BIS = null;
		total = 0;
		stopped = 0;
		canResume = false;
		setState("stop");
	}

	public boolean canResume() {
		return canResume;
	}

	public void setMP3(String name) {
		this.mp3 = getClass().getResourceAsStream(name);
	}
	
	public void setMP3(InputStream song) {
		this.mp3 = song;
	}
	
	public int getPosition() {
		try {
			if (FIS!=null) {
				return (total - FIS.available());
			} else if (canResume) {
				return (total - stopped);
			} else {
				return lastPosition;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public int pause() {
		if (FIS!=null) {
			try {
				stopped = FIS.available();
				player.close();
				AudioDock.setSpeakers(false);
				FIS = null;
				BIS = null;
				player = null;
				if (valid)
					canResume = true;
				setState("pause");
				return stopped;
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		} else if (canResume) {
			setState("pause");
			return stopped;
		} else {
			return 0;
		}
	}

	public void resume() {
		if (!canResume)
			return;
		if (play(total - stopped))
			canResume = false;
	}

	public boolean play() {
		return play(-1);
	}
	
	public boolean play(int pos) {
		valid = true;
		canResume = false;
		try {
			FIS = mp3;
			total = mp3.available();
			if (pos > -1)
				FIS.skip(pos);
			BIS = new BufferedInputStream(FIS);
			player = new Player(BIS);
			AudioDock.setSpeakers(true);
			new Thread(new Runnable() {
				public void run() {
					try {
						player.play();
						AudioDock.setSpeakers(false);
						stop();
					} catch (Exception e) {
						System.err.println("Error playing mp3 file");
						valid = false;
					}
				}
			}).start();
		} catch (Exception e) {
			System.err.println("Error playing mp3 file");
			e.printStackTrace();
			valid = false;
		}
		if(valid){
			setState("play");
		}
		return valid;
	}
	
	public void stop() {
		if (player == null) return;
		try {
			lastPosition = getPosition();
			stopped = 0;
			canResume = false;
			player.close();
			AudioDock.setSpeakers(false);
			FIS = null;
			BIS = null;
			player = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		setState("stop");
	}

	public void volume(int vol) {
		
	}

	public void setSong(String song) {
		this.song =song;
		setMP3(song);
		DynamicDeviceSemantics.getInstance().changed();
	}

	public String getSong() {
		return song;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
		DynamicDeviceSemantics.getInstance().changed();
	}
}