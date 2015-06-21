package assets;

import java.util.HashMap;

import misc.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public final class SoundManager {

	private static SoundManager instance;
	
	private final HashMap<String, Sound> SOUND_MAP = new HashMap<String, Sound>();
	
	public static SoundManager getInstance() {
		if(instance == null) {
			instance = new SoundManager();
		}
		
		return instance;
	}
	
	private SoundManager() {
		FileHandle[] soundFiles = null;
		if(Utils.usingAndroidContext()) {
			soundFiles = Gdx.files.internal("sounds").list();
		} else if(Utils.usingDesktopContext()) {
			soundFiles = Gdx.files.internal("./bin/sounds").list();
		}
	
		for(FileHandle file : soundFiles) {
			String filename = file.name();
			addSound(filename);
		}
	}
	
	public Sound getSound(String key) {
		return SOUND_MAP.get(key);
	}
	
	public void playSound(String key) {
		Sound sound = SOUND_MAP.get(key);
		sound.play();
	}
	
	private void addSound(String filename) {
		Sound sound = Gdx.audio.newSound(Gdx.files.internal("sounds/" + filename));
		String key = filename.substring(0, filename.lastIndexOf('.'));
		SOUND_MAP.put(key, sound);
	}
}
