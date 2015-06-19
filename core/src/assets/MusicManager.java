package assets;

public final class MusicManager {

	private static MusicManager instance;
	
	public static MusicManager getInstance() {
		if(instance == null) {
			instance = new MusicManager();
		}
		
		return instance;
	}
	
	private MusicManager() {
		
	}
}
