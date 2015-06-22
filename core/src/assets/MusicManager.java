package assets;

import java.util.HashMap;

import misc.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public final class MusicManager {

	private static MusicManager instance;
	
	private final HashMap<String, Music> MUSIC_MAP = new HashMap<String, Music>();
	private final Array<Music> MUSIC_QUEUE = new Array<Music>();
	
	private Music currMusic;
	
	public static MusicManager getInstance() {
		if(instance == null) {
			instance = new MusicManager();
		}
		
		return instance;
	}
	
	private MusicManager() {
		FileHandle[] musicFiles = null;
		if(Utils.usingAndroidContext()) {
			musicFiles = Gdx.files.internal("music").list();
		} else if(Utils.usingDesktopContext()) {
			musicFiles = Gdx.files.internal("./bin/music").list();
		}
	
		for(FileHandle file : musicFiles) {
			String filename = file.name();
			addMusic(filename);
		}
	}
	
	public void setQueue(Array<String> keys) {
		MUSIC_QUEUE.clear();
		
		for(String key : keys) {
			Music music = MUSIC_MAP.get(key);
			MUSIC_QUEUE.add(music);
		}
	}
	
	
	public void playQueue(float volume, boolean shuffle) {
		if(currMusic != null) {
			currMusic.stop();
		}
		
		if(shuffle) {
			MUSIC_QUEUE.shuffle();
		}
		
		for(Music music : MUSIC_QUEUE) {
			music.setVolume(volume);
			
			music.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(Music music) {
					playNext();
				}				
			});
		}
		
		playNext();
	}
	
	public void play(String key, float volume, boolean loop) {
		if(currMusic != null) {
			currMusic.stop();
		}
		
		currMusic = MUSIC_MAP.get(key);
		currMusic.setVolume(volume);
		currMusic.setLooping(loop);
		currMusic.play();
	}
	
	public void pause() {
		currMusic.pause();
	}
	
	public void stop() {
		currMusic.stop();
	}
	
	private void playNext() {
		currMusic = MUSIC_QUEUE.removeIndex(0);
		MUSIC_QUEUE.add(currMusic);
		
		currMusic.play();
	}

	private void addMusic(String filename) {
		Music music = Gdx.audio.newMusic(Gdx.files.internal("music/" + filename));
		String key = filename.substring(0, filename.lastIndexOf('.'));
		MUSIC_MAP.put(key, music);
	}
}
