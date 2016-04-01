package ch.ethz.inf.vs.wot.demo.android.resources;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import ch.ethz.inf.vs.wot.demo.android.AudioDock;
import ch.ethz.inf.vs.wot.demo.android.R;

public class MP3Player {

	private MediaPlayer player =new MediaPlayer();
	private boolean canResume;
	private int total;
	private int stopped;
	private boolean valid;
	private String song;
	private String state;
	private int lastPosition;

	public MP3Player() {
		setState("stop");
	}

	public boolean canResume() {
		return canResume;
	}

	public int getPosition() {
		if(!player.isPlaying()){
			return lastPosition;
		}
		return player.getCurrentPosition();
	}

	public void pause() {
		lastPosition = getPosition();
		player.pause();
		setState("pause");

	}

	public void resume() {
		player.start();
		setState("play");
	}

	public boolean play() {
		return play(0);
	}
	
	public boolean play(int pos) {
		lastPosition = 0;
		player.seekTo(pos);
		player.start();
		setState("play");
		return valid;
	}
	
	public void stop() {
		lastPosition = getPosition();
		player.stop();
		setState("stop");
	}

	public void volume(int vol) {
		
	}

	public void setSong(String song, int file) throws IOException {
		this.song =song;
		player.release();
		player = MediaPlayer.create(AudioDock.context, file );
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