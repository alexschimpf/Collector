package assets;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public final class TextureManager {

	private static final String[] IMAGE_KEYS = new String[] {		
	};
	
	private static final String[] ANIMATION_KEYS = new String[] {
	};
	
	private static TextureManager instance;
	
	private final TextureAtlas ATLAS = new TextureAtlas(Gdx.files.internal("game.atlas"));
	private final HashMap<String, AtlasRegion> IMAGE_TEXTURE_MAP = new HashMap<String, AtlasRegion>();
	private final HashMap<String, Array<AtlasRegion>> ANIMATION_TEXTURES_MAP = new HashMap<String, Array<AtlasRegion>>();
	
	public static TextureManager getInstance() {
		if(instance == null) {
			instance = new TextureManager();
		}
		
		return instance;
	}
	
	private TextureManager() {
		for(String key : ANIMATION_KEYS) {
			Array<AtlasRegion> textures = ATLAS.findRegions(key);
			ANIMATION_TEXTURES_MAP.put(key, textures);
		}
		
		for(String key : IMAGE_KEYS) {
			AtlasRegion texture = ATLAS.findRegion(key);
			IMAGE_TEXTURE_MAP.put(key, texture);
		}
	}
	
	public Array<AtlasRegion> getAnimationTextures(String key) {
		return ANIMATION_TEXTURES_MAP.get(key);
	}
	
	public Sprite getSprite(String key) {
		AtlasRegion region = IMAGE_TEXTURE_MAP.get(key);
		if(region == null) {
			return null;
		}
		
		return new Sprite(region);
	}
}
