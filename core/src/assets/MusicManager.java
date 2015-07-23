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
	
	private final HashMap<String, Music> _musicMap = new HashMap<String, Music>();
	private final Array<Music> _musicQueue = new Array<Music>();
	
	private Music _currMusic;
	
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
			_addMusic(filename);
		}
	}
	
	public void setQueue(Array<String> keys) {
		_musicQueue.clear();
		
		for(String key : keys) {
			Music music = _musicMap.get(key);
			_musicQueue.add(music);
		}
	}
	
	
	public void playQueue(float volume, boolean shuffle) {
		if(_currMusic != null) {
			_currMusic.stop();
		}
		
		if(shuffle) {
			_musicQueue.shuffle();
		}
		
		for(Music music : _musicQueue) {
			music.setVolume(volume);
			
			music.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(Music music) {
					_playNext();
				}				
			});
		}
		
		_playNext();
	}
	
	public void play(String key, float volume, boolean loop) {
		if(_currMusic != null) {
			_currMusic.stop();
		}
		
		_currMusic = _musicMap.get(key);
		_currMusic.setVolume(volume);
		_currMusic.setLooping(loop);
		_currMusic.play();
	}
	
	public void pause() {
		_currMusic.pause();
	}
	
	public void stop() {
		_currMusic.stop();
	}
	
	private void _playNext() {
		_currMusic = _musicQueue.removeIndex(0);
		_musicQueue.add(_currMusic);
		
		_currMusic.play();
	}

	private void _addMusic(String filename) {
		Music music = Gdx.audio.newMusic(Gdx.files.internal("music/" + filename));
		String key = filename.substring(0, filename.lastIndexOf('.'));
		_musicMap.put(key, music);
	}
}
