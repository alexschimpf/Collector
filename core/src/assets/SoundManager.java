package assets;

public final class SoundManager {

private static SoundManager instance;
	
	public static SoundManager getInstance() {
		if(instance == null) {
			instance = new SoundManager();
		}
		
		return instance;
	}
	
	private SoundManager() {
		
	}
}
