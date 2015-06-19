package assets;

import com.badlogic.gdx.graphics.g2d.Sprite;

public final class TextureManager {

	private static TextureManager instance;
	
	public static TextureManager getInstance() {
		if(instance == null) {
			instance = new TextureManager();
		}
		
		return instance;
	}
	
	private TextureManager() {
		
	}
	
	public Sprite getSprite(String textureKey) {
		return null;
	}
}
